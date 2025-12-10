package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// 쿠폰 검증 요청
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponValidationRequest {
    private Long userId;              // 사용자 ID
    private BigDecimal orderAmount;   // 주문 금액
    private List<Long> bookIds;       // 주문 도서 ID 리스트
}