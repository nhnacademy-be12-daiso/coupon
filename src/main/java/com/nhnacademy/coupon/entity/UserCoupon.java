package com.nhnacademy.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id @GeneratedValue
    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false) // FK
    private Coupon coupon;

    // User 엔티티 pk 타입이 Long 이라고 가정
    @Column(name = "user_created_id",nullable = false)
    private Long userId;

    @Column(name = "used_at") // 쿠폰 사용일
    private LocalDateTime usedAt;

    @Column(name = "issued_at", nullable = false) // 쿠폰 발급일
    private LocalDateTime issuedAt;

    @Column(name = "expired_at", nullable = false) // 쿠폰 만료일
    private LocalDateTime expiredAt;


}
