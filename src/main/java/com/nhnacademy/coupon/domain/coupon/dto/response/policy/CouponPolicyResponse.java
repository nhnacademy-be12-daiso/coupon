package com.nhnacademy.coupon.domain.coupon.dto.response.policy;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
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
        Integer quantity) {
    public static CouponPolicyResponse from(CouponPolicy policy) {
        return CouponPolicyResponse.builder()
                .couponPolicyId(policy.getCouponPolicyId())
                .couponPolicyName(policy.getCouponPolicyName())
                .couponType(policy.getCouponType())
                .discountWay(policy.getDiscountWay())
                .discountAmount(policy.getDiscountAmount())
                .minOrderAmount(policy.getMinOrderAmount())
                .maxDiscountAmount(policy.getMaxDiscountAmount())
                .validDays(policy.getValidDays())
                .validStartDate(policy.getValidStartDate())
                .validEndDate(policy.getValidEndDate())
                .policyStatus(policy.getCouponPolicyStatus())
                .quantity(policy.getQuantity())
                .build();
    }
}