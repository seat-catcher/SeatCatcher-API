package com.sullung2yo.seatcatcher.user.domain;


import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="refresh_tokens", indexes = @Index(name = "idx_refresh_token", columnList = "refresh_token"))
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER) // 주로 refreshToken 가져올 때 사용자 정보랑 같이 가져오므로 Join 해야 쿼리 최적화
    @JoinColumn(name = "user_id")
    private User user; // RefreshToken을 발급한 사용자

    @Column(nullable = false)
    private String refreshToken; // RefreshToken

    @Column(nullable = false)
    private LocalDateTime expiredAt; // RefreshToken 만료 시간

    public Boolean isExpired() { // RefreshToken 만료 여부 확인
        return LocalDateTime.now().isAfter(this.expiredAt); // 현재 시간이 만료 시간보다 이후인지 확인
    }

}
