package com.sullung2yo.seatcatcher.domain.train.entity;

import com.sullung2yo.seatcatcher.common.domain.base.entity.BaseEntity;
import com.sullung2yo.seatcatcher.domain.train.enums.SeatGroupType;
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
@Table(name="train_seat_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"train_code", "car_code", "seat_group_type"})
                // 우리는 좌석 타입도 ELDERLY_A , B 이렇게 철저하게 구분을 하기 때문에 이런 constraint 를 추가할 수 있음.
        }
)
public class TrainSeatGroup extends BaseEntity {

    @Column(name = "train_code")
    private String trainCode;

    @Column(name = "car_code")
    private String carCode;

    @Column(name = "seat_group_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NORMAL_A_14'")
    @Builder.Default
    private SeatGroupType seatGroupType = SeatGroupType.NORMAL_A_14;
    // 좌석 그룹 타입은 다음과 같습니다.
    // 노약자구역 A, 일반구역 A B C , 노약자구역 B

    @OneToMany(mappedBy = "trainSeatGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("seatLocation asc")
    private List<TrainSeat> trainSeat; // Train에 생성된 좌석 정보 엔티티 리스트
}