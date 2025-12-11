//package com.nhnacademy.coupon.domain.coupon.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//
//@Entity
//@Table(name = "category_coupon")
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class CategoryCoupon {
//
//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "category_coupon_id")
//    private Long categoryCouponId;
//
//    @Column(name = "book_category_id")
//    private Long bookCategoryId;
//3
//    @ManyToOne
//    @JoinColumn(name = "coupon_policy_id")
//    private CouponPolicy couponPolicy;
//
//    @Column(name = "coupon_category_name")
//    private String couponCategoryName;
//
//    public CategoryCoupon(CouponPolicy couponPolicy, Long bookCategoryId, String couponCategoryName) {
//        this.couponPolicy = couponPolicy;
//        this.bookCategoryId = bookCategoryId;
//        this.couponCategoryName = couponCategoryName;
//    }
//
//}
