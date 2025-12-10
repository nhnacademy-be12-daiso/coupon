package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponUseRequest {
    private Long userId;              // 사용자 ID
    private Long orderId;             // 주문 ID
    private BigDecimal orderAmount;   // 주문 금액
    private List<Long> bookIds;       // 주문 도서 ID 리스트
}
