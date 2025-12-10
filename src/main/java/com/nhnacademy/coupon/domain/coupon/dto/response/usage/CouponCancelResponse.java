package com.nhnacademy.coupon.domain.coupon.dto.response.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 쿠폰 취소 응답
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponCancelResponse {
    private Long userCouponId;
    private String status;               // 새로운 상태 (CANCELED)
    private String message;
}
