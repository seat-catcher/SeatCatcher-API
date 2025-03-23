package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="train_seat_groups")
public class TrainSeatGroup extends BaseEntity {

    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.
    */

    @ManyToOne
    @JoinColumn(name = "train_car_id", nullable = false)
    private TrainCar trainCar;

    @OneToMany(mappedBy = "trainSeatGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainSeat> trainSeats;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NORMAL_1'")
    @Builder.Default
    private SeatGroupType type = SeatGroupType.NORMAL_A;
    // 좌석 그룹 타입은 다음과 같습니다.
    // 노약자구역 A, 일반구역 A B C , 노약자구역 B
}
