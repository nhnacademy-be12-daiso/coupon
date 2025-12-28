package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "주문에 대한 쿠폰 사용 요청 DTO (다건 쿠폰 적용)")
public record BatchCouponUseRequest(
        @NotNull
        @Schema(description = "주문 ID", example = "202412250001")
        Long orderId,

        @Schema(description = "사용할 쿠폰 목록")
        @Valid
        List<CouponUseRequest> coupons
) {}