package com.nhnacademy.coupon.domain.coupon.dto.response.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.math.BigDecimal;

@Schema(description = "쿠폰 적용 응답")
@Builder
public record CouponApplyResponse(
        Long userCouponId,
        String couponName,
        BigDecimal originalAmount,     // 원래 금액
        BigDecimal discountAmount,     // 할인 금액
        BigDecimal finalAmount        // 최종 금액
) {
}
