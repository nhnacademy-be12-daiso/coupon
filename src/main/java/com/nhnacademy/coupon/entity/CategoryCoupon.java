package com.nhnacademy.coupon.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "category_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_coupon_id")
    private Long categoryCouponId;

    @Column(name = "book_category_id")
    private Long bookCategoryId;

    @ManyToOne
    @JoinColumn(name = "coupon_policy_id")
    private CouponPolicy couponPolicy;

}
