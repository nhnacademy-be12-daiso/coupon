package com.nhnacademy.coupon.domain.coupon.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BookCoupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_coupon_id")
    private Long bookCouponId;

    @Column(name = "book_id")
    private Long bookId;

    @ManyToOne
    @JoinColumn(name = "coupon_policy_id")
    private CouponPolicy couponPolicy;

    @Column(name = "book_title")
    private String bookTitle;

    public BookCoupon(CouponPolicy couponPolicy, Long bookId, String bookTitle) {
        this.couponPolicy = couponPolicy;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }
}
