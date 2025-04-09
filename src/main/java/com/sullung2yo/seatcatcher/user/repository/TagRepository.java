package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.Tag;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findByTagName(UserTagType userTagType);
    Boolean existsByTagName(UserTagType userTagType);
}
