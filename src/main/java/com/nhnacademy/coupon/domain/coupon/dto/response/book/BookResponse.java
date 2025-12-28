package com.nhnacademy.coupon.domain.coupon.dto.response.book;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "도서 기본 정보 응답 DTO")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private Long bookId;
    private String bookName;
    private Long categoryId;
}