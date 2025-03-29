package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="train_seats")
public class TrainSeat extends BaseEntity {

    /*
        BaseEntity 를 상속받아

        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.

        created_at    ::    데이터베이스에 저장된 날짜입니다.

        modified_at    ::    데이터베이스에서 수정된 날짜입니다.

        해당 세 칼럼은 정의하지 않았습니다.
    */

    @ManyToOne
    @JoinColumn(name = "seat_group_id", nullable = false)
    private TrainSeatGroup trainSeatGroup;

    @Column(name = "index", nullable = false)
    private int index; // 좌석 위치와 매핑되는 정보.

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'NORMAL'")
    @Builder.Default
    private SeatType seatType = SeatType.NORMAL; // 노약자석 / 임산부좌석 / 일반좌석 세 가지만 일단 존재합니다.

    @Column(name = "jjim_count")
    private int jjimCount;
}
