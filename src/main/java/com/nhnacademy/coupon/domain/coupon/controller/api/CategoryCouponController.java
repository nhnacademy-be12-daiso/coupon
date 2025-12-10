package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CategoryCoupon;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.repository.CategoryCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequestMapping("/api/coupons/books")
@RestController
@RequiredArgsConstructor
public class CategoryCouponController {

    private final BookServiceClient bookServiceClient;
    private final CategoryCouponRepository categoryCouponRepository;
    private final UserCouponRepository userCouponRepository;

    @GetMapping("/{bookId}/downloadable")
    public ResponseEntity<List<CategoryCouponResponse>> getDownloadableCoupons(
            @PathVariable long bookId, @RequestHeader("X-User-Id") Long userCreatedId){

        // 1. Book 서비스에서 이 책의 카테고리 조회
        BookCategoryResponse bookCategory = bookServiceClient.getBookCategory(bookId);

        // 2. [수정됨] 이미 발급받은 쿠폰 확인용 키 생성 (형식: "정책ID_타겟ID")
        // 예: 정책 7번, 타겟 400(수학) -> "7_400"
        Set<String> issuedCouponKeys = userCouponRepository.findByUserId(userCreatedId)
                .stream()
                .map(uc -> uc.getCouponPolicy().getCouponPolicyId() + "_" + uc.getTargetId())
                .collect(Collectors.toSet());

        // 3. 1단계, 2단계 카테고리의 쿠폰 정책 조회
        List<CategoryCoupon> primaryCoupons =
                categoryCouponRepository.findByBookCategoryId(bookCategory.getPrimaryCategoryId());
        // ex) categoryCouponRepository.findByBookCategoryId(800) 1단계 쿠폰
        List<CategoryCoupon> secondaryCoupons =
                categoryCouponRepository.findByBookCategoryId(bookCategory.getSecondaryCategoryId());
        // ex) categoryCouponRepository.findByBookCategoryId(810) 2단계 쿠폰

        // 4. 필터링 및 변환
        List<CategoryCouponResponse> responses = Stream
                .concat(primaryCoupons.stream(), secondaryCoupons.stream())
                .filter(categoryCoupon -> {
                    CouponPolicy policy = categoryCoupon.getCouponPolicy();

                    // ACTIVE 상태만
                    if (policy.getCouponPolicyStatus() != CouponPolicyStatus.ACTIVE) {
                        return false;
                    }

                    // 수량 체크
                    if (policy.getQuantity() != null && policy.getQuantity() <= 0) {
                        return false;
                    }
                    String candidateKey = policy.getCouponPolicyId() + "_" + categoryCoupon.getBookCategoryId();

                    // 이미 발급받은 쿠폰 제외
                    if (issuedCouponKeys.contains(candidateKey)) {
                        return false; // "7_800"을 이미 가지고 있다면 제외!
                    }

                    return true;
                })
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);

    }
    private CategoryCouponResponse convertToResponse(CategoryCoupon categoryCoupon) {
        CouponPolicy policy = categoryCoupon.getCouponPolicy();

        return CategoryCouponResponse.builder()
                // CategoryCoupon 정보
                .categoryCouponId(categoryCoupon.getCategoryCouponId())
                .bookCategoryId(categoryCoupon.getBookCategoryId())
                .couponCategoryName(categoryCoupon.getCouponCategoryName())

                // CouponPolicy 정보
                .policyInfo(CategoryCouponResponse.CouponPolicyInfo.builder()
                        .couponPolicyId(policy.getCouponPolicyId())
                        .couponPolicyName(policy.getCouponPolicyName())
                        .discountWay(policy.getDiscountWay().name())
                        .couponType(policy.getCouponType())
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
