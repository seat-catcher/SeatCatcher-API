package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayLine;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="trains")
public class Train extends BaseEntity {

    /*
        BaseEntity 를 상속받아

        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.

        해당 세 칼럼은 정의하지 않았습니다.
    */

    @ManyToOne
    @JoinColumn(name = "id")
    private SubwayLine subwayLine;

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TrainCar> trainCars;

    @Column(name="train_code", nullable = false)
    private String trainCode;

    @Column(name="car_count", nullable = false)
    private int carCount;
}
