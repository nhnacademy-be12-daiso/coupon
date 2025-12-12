package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import jakarta.validation.constraints.NotNull;

public record CouponUseItemRequest(
        @NotNull Long bookId,
        @NotNull Long userCouponId
) {
}
