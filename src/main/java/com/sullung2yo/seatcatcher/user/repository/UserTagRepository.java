package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTagRepository extends JpaRepository<UserTag, Long> {
    List<UserTag> findUserTagByUser(User user);
}
