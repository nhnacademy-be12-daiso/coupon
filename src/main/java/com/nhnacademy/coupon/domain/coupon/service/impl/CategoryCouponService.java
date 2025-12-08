package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.CategoryCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CategoryCoupon;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.repository.CategoryCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CategoryCouponService {

    private final CategoryCouponRepository categoryCouponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public UserCouponResponse issueCategoryCoupon(Long userId, CategoryCouponIssueRequest request) {
        // 1. CategoryCoupon 조회 (카테고리 정보 포함!)
        CategoryCoupon categoryCoupon = categoryCouponRepository
                .findById(request.getCouponPolicyId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 쿠폰을 찾을 수 없습니다."));

        CouponPolicy policy = categoryCoupon.getCouponPolicy();

        // 2. 중복 발급 체크
        boolean alreadyIssued = userCouponRepository
                .existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policy.getCouponPolicyId());

        if (alreadyIssued) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        // 3. 수량 체크
        if (policy.getQuantity() != null && policy.getQuantity() <= 0) {
            throw new IllegalStateException("발급 가능한 쿠폰이 없습니다.");
        }

        // 4. 만료일 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = policy.getValidDays() != null
                ? now.plusDays(policy.getValidDays())
                : policy.getValidEndDate() != null
                ? policy.getValidEndDate()
                : now.plusYears(1);

        // 5. UserCoupon 생성 (카테고리 정보 저장!)
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponPolicy(policy)
                .status(CouponStatus.ISSUED)
                .issuedAt(now)
                .expiryAt(expiryAt)
                // ⭐ TODO: UserCoupon에 categoryCouponId 필드 추가 권장
                .build();

        // 6. 수량 차감
        if (policy.getQuantity() != null) {
            policy.decreaseQuantity();
        }

        UserCoupon saved = userCouponRepository.save(userCoupon);

        return convertToUserCouponResponse(saved);
    }

    private UserCouponResponse convertToUserCouponResponse(UserCoupon userCoupon) {
        // 변환 로직...
        return null;
    }
}