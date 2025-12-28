package com.nhnacademy.coupon.domain.coupon.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도서 기준 다운로드 가능 쿠폰 조회를 위한 조건 DTO")
public class BookCouponQuery {

    @Schema(description = "사용자 ID", example = "1000099")
    private final Long userId;

    @Schema(description = "도서 ID", example = "1001")
    private final Long bookId;

    @Schema(description = "도서 1차 카테고리 ID", example = "800")
    private final Long primaryCategoryId;

    @Schema(description = "도서 2차 카테고리 ID", example = "810")
    private final Long secondaryCategoryId;
}
