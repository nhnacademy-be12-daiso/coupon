package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BatchCouponUseRequest(
        @NotNull Long orderId,
        @Valid List<CouponUseRequest> coupons
) {}