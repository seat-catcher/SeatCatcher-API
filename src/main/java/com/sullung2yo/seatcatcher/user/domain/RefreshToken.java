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
@Table(name="refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER) // 주로 refreshToken 가져올 때 사용자 정보랑 같이 가져오므로 Join 해야 쿼리 최적화
    @JoinColumn(name = "user_id")
    private User user; // RefreshToken을 발급한 사용자

    @Column
    private String refreshToken; // RefreshToken

}
