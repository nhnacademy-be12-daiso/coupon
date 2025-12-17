package com.nhnacademy.coupon.domain.coupon.dto.response.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookCategoryResponse {
    private Long bookId;
    private Long primaryCategoryId;    // 1단계 (예: 800)
    private Long secondaryCategoryId;  // 2단계 (예: 810)
}
