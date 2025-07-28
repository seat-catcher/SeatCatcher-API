package com.sullung2yo.seatcatcher.domain.user.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TagException;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.auth.service.AuthService;
//import com.sullung2yo.seatcatcher.domain.auth.service.AuthServiceImpl;
import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;
import com.sullung2yo.seatcatcher.domain.tag.entity.UserTag;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.dto.request.UserInformationUpdateRequest;
import com.sullung2yo.seatcatcher.domain.tag.repository.TagRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.tag.repository.UserTagRepository;
import org.springframework.transaction.annotation.Transactional;
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
//    private final AuthServiceImpl authService;
    private final AuthService authService;

    @Override
    public User getUserWithId(Long userId) throws RuntimeException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));
    }

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
        log.debug(
                "업데이트할 사용자 : {}, {}, {}, {}, {}, {}",
                user.getProviderId(),
                user.getName(),
                user.getCredit(),
                user.getProfileImageNum(),
                user.getDeviceStatus(),
                user.getUserTag().stream().map(userTag -> userTag.getTag().getTagName()).toList()
        );

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
        if (userInformationUpdateRequest.getIsActive() != null) {
            log.debug("사용자 기기 활성 상태 업데이트: {}", userInformationUpdateRequest.getIsActive());
            user.setDeviceStatus(userInformationUpdateRequest.getIsActive());
        }
        if (userInformationUpdateRequest.getHasOnBoarded() != null) {
            log.debug("온보딩 진행 여부 업데이트: {}", userInformationUpdateRequest.getHasOnBoarded());
            user.setHasOnBoarded(userInformationUpdateRequest.getHasOnBoarded());
        }
        if (userInformationUpdateRequest.getAppleAuthorizationCode() != null) {
            log.debug("Apple Authorization Code 업데이트");
            user.setAppleAuthorizationCode(userInformationUpdateRequest.getAppleAuthorizationCode());
        }

        // 태그 정보 업데이트
        List<UserTagType> tags = userInformationUpdateRequest.getTags();
        if (tags != null) {
            // 기존 태그 관계 제거 -> 새로 생성
            userTagRepository.deleteAll(user.getUserTag());
            user.getUserTag().clear();

            log.debug("사용자 태그 업데이트 개수: {}", tags.size());
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

    @Override
    public void deleteUser(String token) throws RuntimeException {
        // 1. 사용자 정보 가져오기
        User user = this.getUserWithToken(token);
        log.info("사용자 계정 삭제 요청. 사용자 ID: {}, 제공자: {}", user.getProviderId(), user.getProvider());

        // 2. Apple 사용자인 경우 토큰 취소 (앱스토어 가이드라인 준수)
        if (user.getProvider() == Provider.APPLE) {
            try {
                log.info("Apple 사용자 토큰 취소 시작");
                authService.revokeAppleToken(user.getAppleAuthorizationCode());
                log.info("Apple 사용자 토큰 취소 완료");
            } catch (Exception e) {
                log.warn("Apple 토큰 취소 실패하지만 계정 삭제 계속 진행: {}", e.getMessage());
                // 토큰 취소 실패해도 사용자 삭제 요청은 거부하면 안돤다고 하네요
            }
        }

        // 3. 관련 데이터 삭제
        // 3-1. 사용자-태그 중간 테이블 삭제
        userTagRepository.deleteAll(user.getUserTag());
        log.debug("사용자-태그 관계 삭제 완료");

        // 3-2. 기타 사용자 관련 데이터 삭제 (필요시 추가)
        // TODO: 알람, 경로 기록, 좌석 예약 등 관련 데이터 삭제 로직 추가

        // 4. 사용자 정보 삭제
        userRepository.delete(user);
        log.info("사용자 계정 삭제 완료. 사용자 ID: {}", user.getProviderId());
    }
}
