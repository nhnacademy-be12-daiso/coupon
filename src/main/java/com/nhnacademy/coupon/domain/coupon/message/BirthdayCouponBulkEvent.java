package com.nhnacademy.coupon.domain.coupon.message;

import java.util.List;

public record BirthdayCouponBulkEvent(
        List<Long> userIds,
        String batchId   // 추적용(옵션)
) { }
