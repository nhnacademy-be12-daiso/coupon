package com.nhnacademy.coupon.domain.coupon.entity.saga;


import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "coupon_outbox")
@EntityListeners(AuditingEntityListener.class) // Auditing 활성화
public class CouponOutbox{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 멱등성 키

    private Long aggregateId;
    private String aggregateType;

    private String topic; // exchange 이름
    private String routingKey;

    // retry_count는 발행 시도 횟수를 기록합니다.
    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    @CreatedDate // 생성 시 자동 기록
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 업데이트 시 자동 기록
    private LocalDateTime updatedAt;

    // 1. JPA 필수: 기본 생성자 (protected 사용 권장)
    protected CouponOutbox() {
    }

    // 2. 새로운 Outbox 메시지 생성을 위한 생성자
    public CouponOutbox(Long aggregateId, String aggregateType, String topic, String routingKey, String payload) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.topic = topic;
        this.routingKey = routingKey;
        this.payload = payload;
        // status와 retryCount는 기본값(PENDING, 0)을 사용합니다.
    }

    // 3. Relay 프로세스를 위한 상태 변경 메서드
    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}