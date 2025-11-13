package com.nhnacademy.Coupon.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Coupons")
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "discount_way")
    private DiscountWay discountWay;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "target_book") // 도서 아이디
    private Long targetBook;

    @Column(name = "is_birthday") // 생일 여부
    private boolean isBirthday;

    @Column(name = "min_order_amount") // 최소 주문 금액
    private Long minOrderAmount;

    @Column(name = "max_discount_amount") // 최대 할인 금액
    private Long maxDiscountAmount;

    @Column(name = "availability_days") // 사용 기간
    private Integer availabilityDays;

    @Column(name = "created_at", updatable = false, nullable = false) // 쿠폰 생성일
    private LocalDateTime createdAt = LocalDateTime.now();

}
