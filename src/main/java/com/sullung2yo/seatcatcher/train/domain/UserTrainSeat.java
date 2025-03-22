package com.sullung2yo.seatcatcher.train.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.user.domain.User;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="user_train_seats")
public class UserTrainSeat extends BaseEntity {

    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.

        이 Entity 는 Mapping Table입니다! User 와 TrainSeat 의
        One To One 관계를 구현하기 위해 생성되었습니다.
    */
    @OneToOne
    @JoinColumn(name = "id")
    private User user;

    @OneToOne
    @JoinColumn(name = "id")
    private TrainSeat trainSeat;
}
