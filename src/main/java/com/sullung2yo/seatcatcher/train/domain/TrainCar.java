package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="train_cars")
public class TrainCar extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @OneToMany(mappedBy = "trainCar", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainSeatGroup> trainSeatGroups;

    @Column(name = "car_code", nullable = false) // 혹시라도 차량 번호에 알파벳이 들어가는 경우를 대비하겠음
    private String carCode;
}
