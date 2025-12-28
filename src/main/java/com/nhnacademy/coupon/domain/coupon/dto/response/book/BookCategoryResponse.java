package com.nhnacademy.coupon.domain.coupon.dto.response.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "도서의 카테고리 정보 응답 DTO")
public class BookCategoryResponse {

    @Schema(description = "도서 ID", example = "1001")
    private Long bookId;

    @Schema(description = "1차 카테고리 ID", example = "800")
    private Long primaryCategoryId;    // 1단계 (예: 800)

    @Schema(description = "2차 카테고리 ID", example = "810")
    private Long secondaryCategoryId;  // 2단계 (예: 810)
}
