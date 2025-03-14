package com.sullung2yo.seatcatcher.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 내부 Identifier

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
    private UserRole role; // 일반 사용자 or 어드민 (Enum)

    @Column(nullable = false)
    private Long credit; // 사용자 보유 크레딧

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // 가입 시간

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 정보 변경 시간

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간
}
