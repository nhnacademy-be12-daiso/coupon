package com.nhnacademy.coupon.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponPolicy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_policy_id")
    private Long couponPolicyId;

    @Column(name = "coupon_policy_name") // 쿠폰 정책 이름
    private String couponPolicyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false) // 쿠폰 정책 종류
    private CouponType couponType;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_way", nullable = false) // 할인 타입
    private DiscountWay discountWay;

    @Column(name = "discount_amount") // 할인 금액
    private BigDecimal discountAmount;

    @Column(name = "min_order_amount") // 최소 주문 금액
    private Long minOrderAmount;

    @Column(name = "max_discount_amount") // 최대 할인 금액
    private Long maxDiscountAmount;

    @Column(name = "valid_days") // 쿠폰 상대 유효 일수
    private Integer validDays;

    @Column(name = "valid_start_date") // 쿠폰 고정 유효기간 시작일
    private LocalDateTime validStartDate;

    @Column(name = "valid_end_date") // 쿠폰 고정 유효기간 끝나는일
    private LocalDateTime validEndDate;

    @Column(name = "quantity")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status")
    private CouponPolicyStatus couponPolicyStatus;



}
