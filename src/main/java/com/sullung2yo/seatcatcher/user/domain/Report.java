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
@Table(name="reports")
public class Report extends BaseEntity {

    @ManyToOne
    @JoinColumn(name="report_user_id", nullable = false)
    private User reportUser;

    @ManyToOne
    @JoinColumn(name="reported_user_id", nullable = false)
    private User reportedUser;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;
}
