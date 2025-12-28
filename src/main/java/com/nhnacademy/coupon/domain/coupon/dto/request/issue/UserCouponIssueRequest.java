package com.nhnacademy.coupon.domain.coupon.dto.request.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "유저 쿠폰 발급 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserCouponIssueRequest {

    @NotNull(message = "쿠폰 정책 ID는 필수입니다.")
    private Long couponPolicyId;
}
