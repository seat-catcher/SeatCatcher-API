package com.sullung2yo.seatcatcher.domain.tag.repository;

import com.sullung2yo.seatcatcher.domain.tag.entity.Tag;
import com.sullung2yo.seatcatcher.domain.tag.enums.UserTagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(UserTagType userTagType);
    boolean existsByTagName(UserTagType userTagType);
}
