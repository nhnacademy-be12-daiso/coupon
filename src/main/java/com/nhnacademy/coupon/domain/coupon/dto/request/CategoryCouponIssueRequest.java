package com.nhnacademy.coupon.domain.coupon.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    // 어떤 카테고리(또는 책) 문맥에서 발급받는지 식별
    @Schema(description = "적용 대상 ID (카테고리 ID 또는 도서 ID)")
    private Long targetId;
}
