package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponPolicyService {
    // 쿠폰 정책 생성
    CouponPolicyResponse createCoupon(CouponPolicyCreateRequest request);

    // 쿠폰 정책 전체 조회
    List<CouponPolicyResponse> couponPolices();

    // 사용자에게 쿠폰 발급
    UserCouponResponse issueCoupon(Long userId, UserCouponIssueRequest request);

    // Welcome 쿠폰 발급
    void issueWelcomeCoupon(Long userId);

    // 쿠폰 사용 (주문 시)
    CouponApplyResponse applyCoupon(Long userCouponId, BigDecimal orderAmount);

    // 쿠폰 할인 계산
    BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount);

    // 사용자 쿠폰 목록 조회
    Page<UserCouponResponse> getUserCoupons(Long userId, Pageable pageable);

    //  사용 가능한 쿠폰 조회
    List<UserCouponResponse> getAvailableCoupons(Long userId);

    // 만료된 쿠폰 처리 (배치)
    void expireCoupons();
}
