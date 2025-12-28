package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "개별 쿠폰 사용 요청 DTO")
public record CouponUseRequest(
        @Schema(description = "사용할 사용자 쿠폰 ID", example = "5000123")
        @NotNull Long userCouponId
) {
}