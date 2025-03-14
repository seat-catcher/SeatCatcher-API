package com.sullung2yo.seatcatcher.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 테이블 식별자

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 레코드 생성 시간 기록

    @Column
    @LastModifiedDate
    private LocalDateTime updatedAt; // 레코드 정보 변경 시간
}
