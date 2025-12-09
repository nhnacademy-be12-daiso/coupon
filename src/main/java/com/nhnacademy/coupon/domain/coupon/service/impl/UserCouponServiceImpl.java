package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponRepository userCouponRepository;

    public UserCouponServiceImpl(UserCouponRepository userCouponRepository) {
        this.userCouponRepository = userCouponRepository;
    }


    // 쿠폰 사용 (주문 시)
    @Transactional
    public CouponApplyResponse applyCoupon(Long userCouponId, BigDecimal orderAmount, List<Long> productTargetIds){
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CouponPolicyNotFoundException("보유한 쿠폰을 찾을 수 없습니다."));

        CouponPolicy couponPolicy = userCoupon.getCouponPolicy();



        // 최소 주문 금액 체크
        if(couponPolicy.getMinOrderAmount() != null &&
                orderAmount.compareTo(BigDecimal.valueOf(couponPolicy.getMinOrderAmount())) < 0){ // 주문 금액이 쿠폰 최소 금액 보다 작으면 예외 처리
            throw new InvalidCouponException(
                    "최소 주문 금액(" + couponPolicy.getMinOrderAmount() + ")을 충족하지 않습니다.");
        }

        Long couponTargetId = userCoupon.getTargetId();

        // 타겟이 지정된 쿠폰(수학 전용 등)인데, 결제 대상(책/카테고리 리스트)에 그 ID가 없다면?
        if (couponTargetId != null && !productTargetIds.contains(couponTargetId)) {
            throw new InvalidCouponException("이 상품에는 적용할 수 없는 쿠폰입니다.");
        }

        // 할인 금액 계산
        BigDecimal discountAmount = calculateDiscount(couponPolicy, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount); // 할인 된 최종 금액

        // 쿠폰 사용 처리
        userCoupon.use();


        return CouponApplyResponse.builder()
                .userCouponId(userCouponId)
                .couponName(couponPolicy.getCouponPolicyName()) // 정책 이름 사용
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    // 사용자 쿠폰 목록 조회
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getUserCoupons(Long userId){
        List<UserCouponResponse> userCouponResponses = userCouponRepository.findByUserId(userId).stream()
                .map(this::convertToUserCouponResponse).toList();
        return userCouponResponses;

    }

    // 사용 가능한 쿠폰 조회
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getAvailableCoupons(Long userId){
        List<UserCoupon> coupons = userCouponRepository
                .findByUserIdAndStatus(userId, CouponStatus.ISSUED);

        return coupons.stream()
                .map(this::convertToUserCouponResponse)
                .toList();
    }

    // 만료된 쿠폰 개수 반환
    @Transactional
    public void expireCoupons(){
        int count = userCouponRepository.bulkExpireCoupons(LocalDateTime.now());
        log.info("만료 처리된 쿠폰 개수: {}", count);
    }


    public BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount) {
        BigDecimal discountAmount;

        if(couponPolicy.getDiscountWay() == DiscountWay.FIXED){
            // 고정 할인 금액
            discountAmount = couponPolicy.getDiscountAmount();
        } else {
            // 퍼센트 할인
            discountAmount = orderAmount
                    .multiply(couponPolicy.getDiscountAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 최대 할인 금액 제한
        if(couponPolicy.getMaxDiscountAmount() != null){
            BigDecimal maxDiscount = BigDecimal.valueOf(couponPolicy.getMaxDiscountAmount());
            if(discountAmount.compareTo(maxDiscount) > 0){ // 할인 금액이 최대 할인 금액보다 크면 최대 할인 금액으로 할인 금액 Fix!
                discountAmount = maxDiscount;
            }
        }

        return discountAmount;
    }



    private UserCouponResponse convertToUserCouponResponse(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .userId(userCoupon.getUserId())
                .couponPolicy(convertToResponse(userCoupon.getCouponPolicy())) // coupon -> couponPolicy
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

}
