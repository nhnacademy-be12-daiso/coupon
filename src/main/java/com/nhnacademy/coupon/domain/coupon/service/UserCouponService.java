package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponCancelRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.SingleCouponApplyRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.SingleCouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public interface UserCouponService {

    // 사용자 쿠폰 목록 조회
    List<UserCouponResponse> getUserCoupons(Long userId);

    // 사용 가능한 쿠폰 조회
    List<UserCouponResponse> getAvailableCoupons(Long userId, Long bookId);

    // 쿠폰 할인 계산
    BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount);

    SingleCouponApplyResponse calculateSingleCoupon(Long userId, @Valid SingleCouponApplyRequest request);

    // 쿠폰 사용 처리
    void useCoupons(Long userId, @Valid BatchCouponUseRequest request);

    // 쿠폰 취소 보상 처리
    void cancelCouponUsage(Long userId, @Valid CouponCancelRequest request);

    UserCouponResponse downloadCoupon(Long userId, Long couponPolicyId);


}
