package com.nhnacademy.coupon.domain.coupon.dto.response.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 쿠폰 사용 처리 응답
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponUseResponse {
    private Long userCouponId;
    private String couponName;
    private BigDecimal discountAmount;   // 실제 할인된 금액
    private LocalDateTime usedAt;        // 사용 일시
    private String status;               // 새로운 상태 (USED)
}
