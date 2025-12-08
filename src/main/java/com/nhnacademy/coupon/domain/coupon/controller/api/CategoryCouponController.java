package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CategoryCoupon;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.repository.CategoryCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


@RequestMapping("/api/coupons/books")
@RestController
@RequiredArgsConstructor
public class CategoryCouponController {

    private final BookServiceClient bookServiceClient;
    private final CategoryCouponRepository categoryCouponRepository;
    private final UserCouponRepository userCouponRepository;

    @GetMapping("/{bookId}/downloadable")
    public ResponseEntity<List<CouponPolicyResponse>> getDownloadableCoupons(
            @PathVariable long bookId, @RequestHeader("X-User-Id") Long userCreatedId){

        // 1. Book 서비스에서 이 책의 카테고리 조회
        BookCategoryResponse bookCategory = bookServiceClient.getBookCategory(bookId);
        
        // 2. 1단계, 2단계 카테고리의 쿠폰 정책 조회
        List<CouponPolicyResponse> policies = new ArrayList<>();
        
        // 1단계 카테고리 쿠폰
        List<CategoryCoupon> primaryCoupons =
                categoryCouponRepository.findByBookCategoryId(bookCategory.getPrimaryCategoryId());
        // ex) categoryCouponRepository.findByBookCategoryId(800) 1단계 쿠폰
        // 2단계 카테고리 쿠폰
        List<CategoryCoupon> secondaryCoupons =
                categoryCouponRepository.findByBookCategoryId(bookCategory.getSecondaryCategoryId());
        // ex) categoryCouponRepository.findByBookCategoryId(810) 2단계 쿠폰
        // 3. ACTIVE 상태이고 수량이 남은 것만 필터링
        Stream.concat(primaryCoupons.stream(), secondaryCoupons.stream())
                .map(CategoryCoupon::getCouponPolicy)
                .filter(policy -> policy.getCouponPolicyStatus() == CouponPolicyStatus.ACTIVE)
                .filter(policy -> policy.getQuantity() == null || policy.getQuantity() > 0)
                .filter(policy -> {
                    // 이미 발급받은 쿠폰은 제외
                    return !userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(
                            userCreatedId, policy.getCouponPolicyId());
                })
                .distinct()
                .forEach(policy -> policies.add(convertToResponse(policy)));

        return ResponseEntity.ok(policies);

    }
    private CouponPolicyResponse convertToResponse(CouponPolicy policy) {
        return CouponPolicyResponse.builder()
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
                .policyStatus(policy.getCouponPolicyStatus())
                .quantity(policy.getQuantity())
                .build();
    }
}
