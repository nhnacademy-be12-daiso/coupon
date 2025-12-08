package com.nhnacademy.coupon.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCouponIssueRequest {

    @NotNull(message = "쿠폰 정책 ID는 필수입니다.")
    private Long couponPolicyId;
}
