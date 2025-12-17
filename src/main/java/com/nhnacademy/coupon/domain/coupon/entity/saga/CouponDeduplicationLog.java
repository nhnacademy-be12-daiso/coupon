package com.nhnacademy.coupon.domain.coupon.entity.saga;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon_deduplication_log")
@EntityListeners(AuditingEntityListener.class) // 생성일자 자동 기록을 위해 사용
public class CouponDeduplicationLog {

    @Id
    @Column(name = "message_id", length = 128) // RabbitMQ 메시지 ID 또는 이벤트 ID를 가정
    private Long messageId;

    @CreatedDate
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    public CouponDeduplicationLog(Long messageId) {
        this.messageId = messageId;
        this.receivedAt = LocalDateTime.now(); // @CreatedDate가 아닌 경우 수동으로 설정
    }
}