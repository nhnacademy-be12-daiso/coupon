package com.nhnacademy.coupon.domain.coupon.dto.request.policy;

import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponPolicyUpdateRequest {

    @NotBlank(message = "쿠폰 정책 이름은 필수입니다")
    private String couponPolicyName;

    @NotNull(message = "쿠폰 타입은 필수입니다")
    private CouponType couponType;

    @NotNull(message = "할인 방식은 필수입니다")
    private DiscountWay discountWay;

    @NotNull(message = "할인 금액은 필수입니다")
    private BigDecimal discountAmount;

    private Long minOrderAmount;

    private Long maxDiscountAmount;

    private Integer validDays;

    private LocalDateTime validStartDate;

    private LocalDateTime validEndDate;

    private Integer quantity;

    // ⭐ 상태 추가
    @NotNull(message = "정책 상태는 필수입니다")
    private CouponPolicyStatus policyStatus;
}