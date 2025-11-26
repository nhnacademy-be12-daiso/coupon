package com.nhnacademy.coupon.domain.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "사용자 쿠폰 발급 요청")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserCouponIssueRequest {
//    @NotNull(message = "사용자 ID는 필수입니다.")
//    private Long userId;
//     userId는 받을 필요가 없다. 이유는 토큰에서 받기 때문이다.

    @NotNull(message = "쿠폰 ID는 필수입니다.")
    private Long couponPolicyId;
}
