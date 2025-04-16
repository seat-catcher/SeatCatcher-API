package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.common.exception.TagException;
import com.sullung2yo.seatcatcher.user.domain.Tag;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import com.sullung2yo.seatcatcher.user.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getTags() {
        // Given
        Tag tag1 = Tag.builder()
                .tagName(UserTagType.USERTAG_LOWHEALTH)
                .build();
        Tag tag2 = Tag.builder()
                .tagName(UserTagType.USERTAG_PREGNANT)
                .build();

        // When
        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));
        List<Tag> tags = tagService.getTags();

        // Then
        assertNotNull(tags);
        assertEquals(2, tags.size());
        assertEquals(UserTagType.USERTAG_LOWHEALTH, tags.get(0).getTagName());
        assertEquals(UserTagType.USERTAG_PREGNANT, tags.get(1).getTagName());
    }

    @Test
    void getTagById() {
        // Given
        Tag tag1 = Tag.builder()
                .tagName(UserTagType.USERTAG_LOWHEALTH)
                .build();

        // When
        when(tagRepository.findById(any(Long.class))).thenReturn(Optional.of(tag1)); // 아이디 설정할수가 없으니까 tag1 반환하는걸로 가정
        Tag tag = tagService.getTagById(1L);

        // Then
        assertNotNull(tag);
        assertEquals(tag.getTagName(), tag1.getTagName());
    }

    @Test
    void getTagById_ShouldRaiseTagException() {
        // given
        Long invalidId = 999L; // 존재하지 않는 ID

        // when, then
        assertThrows(TagException.class, () -> {
            tagService.getTagById(invalidId);
        });
    }
}