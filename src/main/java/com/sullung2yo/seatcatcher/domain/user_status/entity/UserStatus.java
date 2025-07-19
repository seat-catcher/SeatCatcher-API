package com.sullung2yo.seatcatcher.domain.user_status.entity;

import com.sullung2yo.seatcatcher.common.domain.base.entity.BaseEntity;
import com.sullung2yo.seatcatcher.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_status")
public class UserStatus extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // 어떤 유저의 상태를 나타내는지에 대한 FK 참조

    @Column(name = "train_code")
    private String trainCode; // 유저가 마지막으로 타고 있었던 열차 코드

    @Column(name = "car_code")
    private String carCode; // 유저가 마지막으로 타고 있었던 차량 코드

    @Column(name = "seat_section")
    private String seatSection; // 유저가 마지막으로 타고 있었던 좌석 그룹 종류

    @Column(name = "seat_id_requested")
    private Long seatIdRequested; // 자신이 양보를 요청했던 좌석의 ID

}
