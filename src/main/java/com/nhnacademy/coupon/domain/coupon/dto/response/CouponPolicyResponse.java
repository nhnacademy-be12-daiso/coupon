package com.nhnacademy.coupon.domain.coupon.dto.response;

import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "쿠폰 정책 응답")
@Builder
public record CouponPolicyResponse(
        Long couponPolicyId,
        String couponPolicyName,
        CouponType couponType,
        DiscountWay discountWay,
        BigDecimal discountAmount,
        Long minOrderAmount,
        Long maxDiscountAmount,
        Integer validDays,
        LocalDateTime validStartDate,
        LocalDateTime validEndDate,
        CouponPolicyStatus policyStatus,
        Integer quantity
) {
}