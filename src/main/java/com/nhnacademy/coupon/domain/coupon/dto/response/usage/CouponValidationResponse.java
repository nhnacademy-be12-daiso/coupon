package com.nhnacademy.coupon.domain.coupon.dto.response.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 쿠폰 검증 응답
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponValidationResponse {
    private boolean valid;               // 검증 성공 여부
    private String message;              // 실패 시 사유
    private BigDecimal expectedDiscount; // 예상 할인 금액
}