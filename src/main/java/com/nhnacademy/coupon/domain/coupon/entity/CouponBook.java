package com.nhnacademy.coupon.domain.coupon.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon_books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_book_id")
    private Long couponBookId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_policy_id", nullable = false)
    private CouponPolicy couponPolicy;

    @Column(name = "book_id", nullable = false)
    private Long bookId;


    private CouponBook(CouponPolicy couponPolicy, Long bookId) {
        this.couponPolicy = couponPolicy;
        this.bookId = bookId;
    }

    public static CouponBook of(CouponPolicy policy, Long bookId) {
        return new CouponBook(policy, bookId);
    }
}
