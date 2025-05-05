package com.sullung2yo.seatcatcher.user.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_alarm")
public class UserAlarm extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 알람을 소유한 유저

    @Column(name = "notification_type")
    private PushNotificationType type;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "is_read")
    private boolean isRead; // 사용자의 열람 여부
}
