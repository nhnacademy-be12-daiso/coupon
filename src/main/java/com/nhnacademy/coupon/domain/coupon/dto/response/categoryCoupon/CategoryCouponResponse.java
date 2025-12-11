package com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCouponResponse {

    private Long bookCategoryId;        // 이 쿠폰이 매핑된 카테고리 ID (예: 400)
    private String couponCategoryName;  // 자연과학, 수학 등 (지금은 null로 둬도 됨)
    private CouponPolicyInfo policyInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponPolicyInfo {
        private Long couponPolicyId;
        private String couponPolicyName;
        private CouponType couponType;
        private DiscountWay discountWay;
        private BigDecimal discountAmount;
        private Long minOrderAmount;
        private Long maxDiscountAmount;
        private Integer validDays;
        private LocalDateTime validStartDate;
        private LocalDateTime validEndDate;
        private Integer quantity;
    }

    // ★ 쿠폰 정책 + 카테고리 ID 만으로 응답 만들기
    public static CategoryCouponResponse of(CouponPolicy policy, Long categoryId) {
        return CategoryCouponResponse.builder()
                .bookCategoryId(categoryId)
                .couponCategoryName(null) // 지금은 이름 없이 ID만, 나중에 Book 서비스에서 이름 채워도 됨
                .policyInfo(CouponPolicyInfo.builder()
                        .couponPolicyId(policy.getCouponPolicyId())
                        .couponPolicyName(policy.getCouponPolicyName())
                        .couponType(policy.getCouponType())
                        .discountWay(policy.getDiscountWay())
                        .discountAmount(policy.getDiscountAmount())
                        .minOrderAmount(policy.getMinOrderAmount())
                        .maxDiscountAmount(policy.getMaxDiscountAmount())
                        .validDays(policy.getValidDays())
                        .validStartDate(policy.getValidStartDate())
                        .validEndDate(policy.getValidEndDate())
                        .quantity(policy.getQuantity())
                        .build())
                .build();
    }
}
