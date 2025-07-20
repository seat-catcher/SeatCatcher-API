package com.sullung2yo.seatcatcher.domain.alarm.repository;

import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.alarm.entity.UserAlarm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAlarmRepository extends JpaRepository<UserAlarm, Long> {
    @Query("""
    SELECT a FROM UserAlarm a
    WHERE a.user = :user
    AND (:type IS NULL OR a.type = :type)
    AND (:isRead IS NULL OR a.isRead = :isRead)
    AND (:cursor IS NULL OR a.id < :cursor)
    ORDER BY a.id DESC
    """)
    List<UserAlarm> findScrollByUserAndCursor(
            @Param("user") User user,
            @Param("type") PushNotificationType type,
            @Param("isRead") Boolean isRead,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
