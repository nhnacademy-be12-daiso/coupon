package com.nhnacademy.coupon.domain.coupon.dto.response;

import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "사용자 쿠폰 응답")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCouponResponse {
    private Long userCouponId;
    private Long userId;
    private CouponPolicyResponse coupon;
    private CouponStatus status;
    private LocalDateTime issuedAt; // 발급 일자
    private LocalDateTime expiryAt; // 쿠폰 만료 일자
    private LocalDateTime usedAt; // 쿠폰 사용일
}