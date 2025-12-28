package com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "도서/카테고리 기준 다운로드 가능 쿠폰 응답 DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCouponResponse {

    @Schema(description = "쿠폰이 적용되는 카테고리 ID", example = "400")
    private Long bookCategoryId;

    @Schema(description = "카테고리 이름 (현재는 null, 추후 Book 서비스 연동 예정)", example = "자연과학")
    private String couponCategoryName;

    @Schema(description = "쿠폰 정책 상세 정보")
    private CouponPolicyInfo policyInfo;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "쿠폰 정책 정보")
    public static class CouponPolicyInfo {

        @Schema(description = "쿠폰 정책 ID", example = "7")
        private Long couponPolicyId;

        @Schema(description = "쿠폰 정책 이름", example = "카테고리 20% 할인 쿠폰")
        private String couponPolicyName;

        @Schema(description = "쿠폰 타입", example = "CATEGORY")
        private CouponType couponType;

        @Schema(description = "할인 방식 (AMOUNT / RATE)", example = "RATE")
        private DiscountWay discountWay;

        @Schema(description = "할인 금액 (정액 또는 퍼센트)", example = "20")
        private BigDecimal discountAmount;

        @Schema(description = "최소 주문 금액 조건", example = "30000")
        private Long minOrderAmount;

        @Schema(description = "최대 할인 금액", example = "5000")
        private Long maxDiscountAmount;

        @Schema(description = "쿠폰 유효 기간(일)", example = "30")
        private Integer validDays;

        @Schema(description = "쿠폰 발급 시작일", example = "2025-01-01T00:00:00")
        private LocalDateTime validStartDate;

        @Schema(description = "쿠폰 발급 종료일", example = "2025-01-31T23:59:59")
        private LocalDateTime validEndDate;

        @Schema(description = "쿠폰 발급 수량", example = "1000")
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
