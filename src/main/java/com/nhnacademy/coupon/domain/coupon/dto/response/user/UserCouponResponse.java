package com.nhnacademy.coupon.domain.coupon.dto.response.user;

import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
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
    private CouponPolicyResponse couponPolicy;
    private CouponStatus status;
    private LocalDateTime issuedAt; // 발급 일자
    private LocalDateTime expiryAt; // 쿠폰 만료 일자
    private LocalDateTime usedAt; // 쿠폰 사용일
    private String itemName; // 카테고리/도서 쿠폰 view를 위한 컬럼

    public static UserCouponResponse from(UserCoupon uc) {
        return UserCouponResponse.builder()
                .userCouponId(uc.getUserCouponId())
                .userId(uc.getUserId())
                .couponPolicy(CouponPolicyResponse.from(uc.getCouponPolicy()))
                .status(uc.getStatus())
                .issuedAt(uc.getIssuedAt())
                .expiryAt(uc.getExpiryAt())
                .usedAt(uc.getUsedAt())
                .itemName(null)
                .build();
    }
}