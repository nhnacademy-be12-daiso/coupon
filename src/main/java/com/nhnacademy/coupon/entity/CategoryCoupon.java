package com.nhnacademy.coupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_coupons")
@Getter
@NoArgsConstructor
public class CategoryCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_coupon_id")
    private Long categoryCouponId;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "category_id")
    private Long categoryId;

}
