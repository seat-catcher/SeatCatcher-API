package com.sullung2yo.seatcatcher.user.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(
            name = "uk_provider_provider_id",
            columnNames = {"provider", "providerId"}
    )
)
public class User extends BaseEntity {

    @Column
    private String email; // 이메일

    @Column
    private String password; // 비밀번호 -> Only for Admin

    @Column(nullable = false)
    private String name; // 사용자 이름 -> Random Nickname

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider; // 인증 제공자 (Enum)

    @Column(nullable = false)
    private String providerId; // 인증 제공자에서 받은 ID

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ROLE_USER'") // default role = ROLE_USER
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER; // 권한 레벨

    @Column(nullable = false)
    @ColumnDefault("0")
    @Min(value = 0L, message = "크레딧은 0 이상이어야 합니다.")
    @Builder.Default
    private Long credit = 0L; // 사용자 보유 크레딧

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private UserTag tag;

    @Column
    private String profileImageUrl;

    @Column
    private String backgroundColor;
}
