package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.Tag;
import com.sullung2yo.seatcatcher.user.domain.UserTagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(UserTagType userTagType);
    boolean existsByTagName(UserTagType userTagType);
}
