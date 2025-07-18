package com.sullung2yo.seatcatcher.domain.tag.repository;

import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.domain.tag.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findUserTagByUser(User user);
}
