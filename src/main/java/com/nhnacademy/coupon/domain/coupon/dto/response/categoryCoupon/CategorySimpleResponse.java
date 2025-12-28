package com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon;


import io.swagger.v3.oas.annotations.media.Schema;

// coupon 서버
@Schema(description = "카테고리 기본 정보 응답 DTO")
public record CategorySimpleResponse(

        @Schema(description = "카테고리 ID", example = "800")
        Long categoryId,

        @Schema(description = "카테고리 이름", example = "자연과학")
        String categoryName
) {
}