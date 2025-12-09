package com.nhnacademy.coupon.domain.coupon.dto.response;

import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CategoryCouponResponse(
        // CategoryCoupon 정보
        Long categoryCouponId, // pk
        Long bookCategoryId,
        String couponCategoryName,

        // CouponPolicy 정보 (내장)
        CouponPolicyInfo policyInfo
) {
    @Builder
    public record CouponPolicyInfo(
            Long couponPolicyId,
            String couponPolicyName,
            String discountWay,
            CouponType couponType,
            BigDecimal discountAmount,
            Long minOrderAmount,
            Long maxDiscountAmount,
            Integer validDays,
            LocalDateTime validStartDate,
            LocalDateTime validEndDate,
            Integer quantity
    ) {}
}