package com.nhnacademy.coupon.domain.coupon.dto.response.policy;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableCouponResponse {

    private Long couponPolicyId;
    private String couponPolicyName;
    private String couponType;      // GENERAL, CATEGORY, BOOKS...
    private boolean downloaded;     // 이미 다운로드했는지 여부

    public static AvailableCouponResponse of(CouponPolicy policy, boolean downloaded) {
        return AvailableCouponResponse.builder()
                .couponPolicyId(policy.getCouponPolicyId())
                .couponPolicyName(policy.getCouponPolicyName())
                .couponType(policy.getCouponType().name())
                .downloaded(downloaded)
                .build();
    }
}
