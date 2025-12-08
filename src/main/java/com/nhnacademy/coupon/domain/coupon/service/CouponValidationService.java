package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.entity.CategoryCoupon;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.repository.CategoryCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponValidationService {

    private final UserCouponRepository userCouponRepository;
    private final CategoryCouponRepository categoryCouponRepository;
    private final BookServiceClient bookServiceClient;

    public boolean validateCoupon(Long userCouponId, List<Long> bookIds, Long userId) {

        // 1. UserCoupon 조회
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        // 2. 기본 검증
        if (!userCoupon.getUserId().equals(userId)) {
            return false;
        }

        if (!userCoupon.isAvailable()) {
            return false;
        }

        CouponPolicy policy = userCoupon.getCouponPolicy();

        // 3. 타입별 검증
        if (policy.getCouponType() == CouponType.CATEGORY) {
            return validateCategoryCoupon(policy, bookIds);
        }

        // GENERAL, WELCOME, BIRTHDAY는 모두 허용
        return true;
    }

    /**
     * 카테고리 쿠폰 검증
     * UserCoupon → CouponPolicy → CategoryCoupon 순회
     */
    private boolean validateCategoryCoupon(CouponPolicy policy, List<Long> bookIds) {

        // 이 정책이 적용되는 카테고리 ID 조회
        List<CategoryCoupon> categoryCoupons =
                categoryCouponRepository.findByCouponPolicy_CouponPolicyId(
                        policy.getCouponPolicyId()
                );

        Set<Long> allowedCategoryIds = categoryCoupons.stream()
                .map(CategoryCoupon::getBookCategoryId)
                .collect(Collectors.toSet());

        log.info("허용된 카테고리: {}", allowedCategoryIds);

        // 주문하는 모든 도서가 허용된 카테고리에 속하는지 확인
        for (Long bookId : bookIds) {
            var bookCategory = bookServiceClient.getBookCategory(bookId);

            boolean matches = allowedCategoryIds.contains(bookCategory.getPrimaryCategoryId())
                    || allowedCategoryIds.contains(bookCategory.getSecondaryCategoryId());

            if (!matches) {
                log.warn("카테고리 불일치: bookId={}, bookCategories={}/{}, allowed={}",
                        bookId,
                        bookCategory.getPrimaryCategoryId(),
                        bookCategory.getSecondaryCategoryId(),
                        allowedCategoryIds);
                return false;
            }
        }

        return true;
    }
}