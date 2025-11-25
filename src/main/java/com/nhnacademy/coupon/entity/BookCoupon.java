package com.nhnacademy.coupon.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Book_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_coupon_id")
    private Long bookCouponId;

    @Column(name = "book_id")
    private Long bookId;

    @ManyToOne
    @JoinColumn(name = "coupon_policy_id")
    private CouponPolicy couponPolicy;
}
