package com.sullung2yo.seatcatcher.domain.tag.utility.initializer;

import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import com.sullung2yo.seatcatcher.domain.tag.repository.TagRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class TagInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;

    @Override
    public void run(String... args) throws Exception {
        // TODO : 추후 이걸 Production에도 올릴 때 실행시킬지 말지 결정해야 함
        try {
            log.info("태그 정보 초기화...");
            UserTagType[] tagTypes = UserTagType.values();
            List<Tag> tagCreationList = new ArrayList<>();
            for (UserTagType userTagType : tagTypes) {
                if (!tagRepository.existsByTagName(userTagType)) {
                    tagCreationList.add(Tag.builder().tagName(userTagType).build());
                }
            }
            tagRepository.saveAll(tagCreationList);
            log.info("태그 정보 초기화 완료 : {}", tagCreationList.size());
        } catch (Exception e) {
            log.error("태그 정보 초기화 중 오류 발생: {}", e.getMessage());
            throw e;
        }
    }
}
