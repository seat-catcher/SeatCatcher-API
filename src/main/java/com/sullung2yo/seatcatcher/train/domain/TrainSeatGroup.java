package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="train_seat_groups")
public class TrainSeatGroup extends BaseEntity {

/*
    // Old
    @ManyToOne
    @JoinColumn(name = "train_car_id", nullable = false)
    private TrainCar trainCar;
 */

    @OneToMany(mappedBy = "trainSeatGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seatLocation asc")
    private List<TrainSeat> trainSeats;

    @Column(name = "train_code")
    private String trainCode;

    @Column(name = "car_code")
    private String carCode;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NORMAL_A_14'")
    @Builder.Default
    private SeatGroupType type = SeatGroupType.NORMAL_A_14;
    // 좌석 그룹 타입은 다음과 같습니다.
    // 노약자구역 A, 일반구역 A B C , 노약자구역 B
}
