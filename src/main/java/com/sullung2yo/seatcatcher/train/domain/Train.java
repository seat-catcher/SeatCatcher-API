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
@Table(name="train")
public class Train extends BaseEntity {

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

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("seatLocation asc")
    private List<TrainSeat> trainSeat; // Train에 생성된 좌석 정보 엔티티 리스트
}
