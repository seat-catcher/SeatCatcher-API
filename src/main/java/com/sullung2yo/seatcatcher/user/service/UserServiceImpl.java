package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TagException;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.config.exception.UserException;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.*;
import com.sullung2yo.seatcatcher.user.dto.request.UserInformationUpdateRequest;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.repository.UserTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;
    private final TagRepository tagRepository;
    private final UserTagRepository userTagRepository;

    @Override
    public User getUserWithToken(String token) throws RuntimeException {
        // 1. 토큰 검증
        jwtTokenProvider.validateToken(token, TokenType.ACCESS);

        // 2. 토큰에서 사용자 정보 추출
        String providerId = jwtTokenProvider.getProviderIdFromToken(token);
        if (providerId == null || providerId.isEmpty()) {
            throw new TokenException("토큰에서 사용자 정보를 추출할 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 3. 사용자 정보 DB에서 조회 후 반환
        Optional<User> user = userRepository.findByProviderId(providerId);
        if (user.isEmpty()) {
            throw new UserException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        return user.get();
    }

    @Override
    public User updateUser(String token, UserInformationUpdateRequest userInformationUpdateRequest) throws RuntimeException {
        // 1. 사용자 정보 업데이트
        log.debug("사용자 정보 업데이트 요청: {}", userInformationUpdateRequest.toString());
        User user = this.getUserWithToken(token);

        // 기본 정보 업데이트
        if (userInformationUpdateRequest.getName() != null) {
            log.debug("사용자 이름 업데이트: {}", userInformationUpdateRequest.getName());
            user.setName(userInformationUpdateRequest.getName());
        }
        if (userInformationUpdateRequest.getProfileImageNum() != null) {
            log.debug("사용자 프로필 이미지 업데이트: {}", userInformationUpdateRequest.getProfileImageNum());
            if (!List.of(ProfileImageNum.values()).contains(userInformationUpdateRequest.getProfileImageNum())) {
                throw new UserException("올바른 프로필 이미지 번호가 아닙니다.", ErrorCode.INVALID_PROFILE_IMAGE_NUM);
            }
            user.setProfileImageNum(userInformationUpdateRequest.getProfileImageNum());
        }
        if (userInformationUpdateRequest.getCredit() != null) {
            log.debug("사용자 크레딧 업데이트: {}", userInformationUpdateRequest.getCredit());
            user.setCredit(userInformationUpdateRequest.getCredit());
        }

        // 태그 정보 업데이트
        log.debug("사용자 태그 업데이트: {}", userInformationUpdateRequest.getTags());
        List<UserTagType> tags = userInformationUpdateRequest.getTags();
        if (tags != null) {
            // 기존 태그 관계 제거
            user.getUserTag().clear();

            // 새 태그 관계 설정
            for (UserTagType userTagType : tags) {
                // 이미 존재하는 태그를 찾거나 새로 생성
                Optional<Tag> tag = tagRepository.findByTagName(userTagType);
                if (tag.isEmpty()) {
                    throw new TagException("해당 태그는 올바른 태그가 아닙니다.", ErrorCode.TAG_NOT_FOUND);
                }

                // 새 UserTag 생성 및 양방향 관계 설정
                UserTag userTag = UserTag.builder()
                        .user(user)
                        .tag(tag.get())
                        .build();
                userTag.setRelationships(user, tag.get());

                // 저장
                userTagRepository.save(userTag);
            }
        }

        // 2. DB에 업데이트된 사용자 정보 저장
        userRepository.save(user);

        return user;
    }
}
