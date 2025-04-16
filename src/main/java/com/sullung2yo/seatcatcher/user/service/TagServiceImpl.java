package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TagException;
import com.sullung2yo.seatcatcher.user.domain.Tag;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService{

    private final TagRepository tagRepository;


    @Override
    public List<Tag> getTags() {
        // 데이터베이스에 있는 모든 태그 정보 가져오기
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(Long id) {
        // 태그 ID로 태그 정보 조회
        return tagRepository.findById(id).orElseThrow(() -> new TagException("해당 ID의 태그가 없습니다.", ErrorCode.TAG_NOT_FOUND));
    }

}
