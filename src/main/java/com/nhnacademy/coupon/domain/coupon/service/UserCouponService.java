package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.response.usage.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;

import java.math.BigDecimal;
import java.util.List;

public interface UserCouponService {

    // 쿠폰 사용 (주문 시)
    CouponApplyResponse applyCoupon(Long userCouponId, BigDecimal orderAmount, List<Long> productTargetIds);

    // 사용자 쿠폰 목록 조회
    List<UserCouponResponse> getUserCoupons(Long userId);

    // 사용 가능한 쿠폰 조회
    List<UserCouponResponse> getAvailableCoupons(Long userId, Long bookId);

    // 만료 처리된 쿠폰 개수 조회
    void expireCoupons();

    // 쿠폰 할인 계산
    BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount);
}
