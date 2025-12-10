package com.nhnacademy.coupon.domain.coupon.dto.response.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SingleCouponApplyResponse(
        @Schema(description = "도서 ID")
        Long bookId,

        @Schema(description = "쿠폰 ID")
        Long userCouponId,

        @Schema(description = "쿠폰 이름", example = "생일 축하 쿠폰")
        String couponName,

        @Schema(description = "원래 금액 (가격 x 수량)", example = "20000")
        BigDecimal originalAmount,

        @Schema(description = "할인 금액", example = "2000")
        BigDecimal discountAmount,

        @Schema(description = "최종 금액", example = "18000")
        BigDecimal finalAmount,

        @Schema(description = "적용 가능 여부", example = "true")
        boolean applicable,

        @Schema(description = "메시지 (실패 시 사유)", example = "적용 가능")
        String message
) {
}
