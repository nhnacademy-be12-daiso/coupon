package com.nhnacademy.coupon.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "coupon-policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponPolicy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_policy_id")
    private Long couponPolicyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_way", nullable = false)
    private DiscountWay discountWay;

    @Column(name = "coupon_discount")
    private BigDecimal bigDecimal;

    @Column(name = "min_order_amount") // 최소 주문 금액
    private Long minOrderAmount;

    @Column(name = "max_discount_amount") // 최대 할인 금액
    private Long maxDiscountAmount;

}
