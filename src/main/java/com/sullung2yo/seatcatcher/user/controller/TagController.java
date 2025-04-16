package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.user.domain.Tag;
import com.sullung2yo.seatcatcher.user.dto.response.TagResponse;
import com.sullung2yo.seatcatcher.user.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("")
    public ResponseEntity<List<TagResponse>> getTags() {
        // 데이터베이스에 있는 모든 태그 정보 가져오는 API
        List<Tag> tagList = tagService.getTags();
        if (tagList.isEmpty()) {
            log.warn("태그가 하나도 없습니다.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } else {
            log.info("태그 목록 조회 성공");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(tagList
                        .stream()
                        .map(tag -> TagResponse.builder()
                            .id(tag.getId())
                            .tagName(tag.getTagName())
                            .build()
                        )
                        .toList()
                    );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        // 태그 ID로 태그 정보 조회하는 API
        Tag tag = tagService.getTagById(id);
        log.debug("조회한 태그 이름 : {}", tag.getTagName());
        return ResponseEntity.status(HttpStatus.OK).body(
                TagResponse.builder()
                        .id(tag.getId())
                        .tagName(tag.getTagName())
                        .build()
        );
    }

}
