package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.global.client.BookServiceClient;
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

    @GetMapping("/{bookId}/downloadable")
    public ResponseEntity<List<CategoryCouponResponse>> getDownloadableCoupons(
            @PathVariable long bookId,
            @RequestHeader("X-User-Id") Long userId) {

        // 1. 도서 카테고리 조회
        BookCategoryResponse bookCategory = bookServiceClient.getBookCategory(bookId);

        // 2. 1단계 + 2단계에 대해 다운 가능한 쿠폰 조회
        List<CategoryCouponResponse> responses =
                couponPolicyService.getAvailableCouponsForBook(
                        userId,
                        bookCategory.getPrimaryCategoryId(),
                        bookCategory.getSecondaryCategoryId()
                );

        return ResponseEntity.ok(responses);
    }
}
