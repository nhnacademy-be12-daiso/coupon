package com.nhnacademy.coupon.dto.response;

import com.nhnacademy.coupon.entity.CouponPolicyStatus;
import com.nhnacademy.coupon.entity.CouponType;
import com.nhnacademy.coupon.entity.DiscountWay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "쿠폰 정책 응답")
@Builder
public record CouponResponse(
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
        CouponPolicyStatus status
) {
}