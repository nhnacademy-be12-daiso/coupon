package com.nhnacademy.coupon.domain.coupon.dto.request.usercoupon;

import lombok.Getter;

@Getter
public class CouponDownloadRequest {
    private Long userId;
    private Long couponPolicyId;
}
