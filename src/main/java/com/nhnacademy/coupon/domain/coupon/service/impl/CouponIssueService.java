package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.CategoryCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponIssueService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public UserCouponResponse issueCoupon(Long userId, CategoryCouponIssueRequest request) {

        // 1. CouponPolicy 조회
        CouponPolicy policy = couponPolicyRepository
                .findById(request.getCouponPolicyId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 정책을 찾을 수 없습니다."));

        // 카테고리/도서 쿠폰인데 targetId가 없으면 예외 발생
        CouponType type = policy.getCouponType();
        if ((type == CouponType.CATEGORY || type == CouponType.BOOKS) && request.getTargetId() == null) {

            throw new IllegalArgumentException("이 쿠폰은 적용 대상(targetId)이 반드시 지정되어야 합니다.");
        }
        // 3. [중복 체크] 정책 + 타겟 조합으로 확인
        if (userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyIdAndTargetId(
                userId, policy.getCouponPolicyId(), request.getTargetId())) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        // 4. 수량 체크 및 차감
        policy.decreaseQuantity();


        // 5. 만료일 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = calculateExpiryDate(policy, now);

        // 6. UserCoupon 생성
        UserCoupon userCoupon = UserCoupon.builder()
                .couponPolicy(policy)
                .userId(userId)
                .targetId(request.getTargetId())
                .status(CouponStatus.ISSUED)
                .issuedAt(now)
                .expiryAt(expiryAt)
                .build();

        UserCoupon saved = userCouponRepository.save(userCoupon);

        return convertToUserCouponResponse(saved);
    }

    private UserCouponResponse convertToUserCouponResponse(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .userId(userCoupon.getUserId())
                .couponPolicy(convertToResponse(userCoupon.getCouponPolicy()))
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiryAt(userCoupon.getExpiryAt()) // expiryAt으로 통일
                .usedAt(userCoupon.getUsedAt())
                .build();
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


    private LocalDateTime calculateExpiryDate(CouponPolicy policy, LocalDateTime now) {
        if (policy.getValidDays() != null) {
            return now.plusDays(policy.getValidDays());
        } else if (policy.getValidEndDate() != null) {
            return policy.getValidEndDate();
        } else {
            return now.plusYears(1);
        }
    }
}
