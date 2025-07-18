package com.sullung2yo.seatcatcher.user.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.domain.auth.entity.RefreshToken;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column
    private String appleAuthorizationCode; // Apple 인증 시에만 사용되는 Authorization Code

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ROLE_USER'") // default role = ROLE_USER
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER; // 권한 레벨

    @Column
    private String fcmToken; // 기기별 고유의 fcm 토큰입니다.

    @Column(nullable = false)
    @Builder.Default
    @ColumnDefault("true") // 회원 가입 시 -> 기기 상태는 foreground 이므로 기본값을 true로 설정
    private Boolean deviceStatus = true; // 기기 상태 (true: foreground, false: background)

    @Column(nullable = false)
    @ColumnDefault("0")
    @Min(value = 0L, message = "크레딧은 0 이상이어야 합니다.")
    @Builder.Default
    private Long credit = 0L; // 사용자 보유 크레딧

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserTag> userTag = new HashSet<>(); // 사용자 태그 (M:N 관계 -> UserTag Entity 참조)

    @Column
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'IMAGE_1'")
    private ProfileImageNum profileImageNum; // 프로필 이미지 번호 (이미지 자체는 프론트에서 관리)

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @Column(nullable = false)
    @ColumnDefault("false")
    @Builder.Default
    Boolean hasOnBoarded = false; // 온보딩 진행 여부 (true: 온보딩 완료, false: 온보딩 미진행)
}
