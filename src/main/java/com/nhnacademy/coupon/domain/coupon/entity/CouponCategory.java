package com.nhnacademy.coupon.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coupon_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CouponCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_category_id")
    private Long couponCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_policy_id", nullable = false)
    private CouponPolicy couponPolicy;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    public static CouponCategory of(CouponPolicy policy, Long categoryId) {
        return CouponCategory.builder()
                .couponPolicy(policy)
                .categoryId(categoryId)
                .build();
    }
}
