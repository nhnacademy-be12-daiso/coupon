package com.nhnacademy.coupon.domain.coupon.dto.request.issue;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserCouponIssueRequest {

    @NotNull(message = "쿠폰 정책 ID는 필수입니다.")
    private Long couponPolicyId;
}
