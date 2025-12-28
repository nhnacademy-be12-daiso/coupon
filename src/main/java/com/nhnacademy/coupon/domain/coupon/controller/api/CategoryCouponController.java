package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.query.BookCouponQuery;
import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/api/coupons/books")
@RestController
@RequiredArgsConstructor
public class CategoryCouponController {

    private final BookServiceClient bookServiceClient;
    private final CouponPolicyService couponPolicyService;

    @Operation(summary = "도서 다운로드 가능 쿠폰 조회", description = "사용자/도서/카테고리 기준으로 다운로드 가능한 쿠폰 목록을 반환합니다.")
    @GetMapping("/{bookId}/downloadable")
    public ResponseEntity<List<CategoryCouponResponse>> getDownloadableCoupons(
            @PathVariable long bookId,
            @RequestHeader("X-User-Id") Long userId) {

        // 1. 도서 카테고리 조회
        BookCategoryResponse bookCategory = bookServiceClient.getBookCategory(bookId);

        // 2. 조회 조건 DTO 구성
        BookCouponQuery query = BookCouponQuery.builder()
                .userId(userId)
                .bookId(bookId)
                .primaryCategoryId(bookCategory.getPrimaryCategoryId())
                .secondaryCategoryId(bookCategory.getSecondaryCategoryId())
                .build();
        // 3. 서비스 호출
        List<CategoryCouponResponse> responses = couponPolicyService.getAvailableCouponsForBook(query);

        return ResponseEntity.ok(responses);
    }
}
