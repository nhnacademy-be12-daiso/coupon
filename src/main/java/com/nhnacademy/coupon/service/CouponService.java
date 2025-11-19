package com.nhnacademy.coupon.service;

import com.nhnacademy.coupon.dto.request.CouponCreateRequest;
import com.nhnacademy.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.dto.response.CouponResponse;
import com.nhnacademy.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    // 쿠폰 정책 생성
    CouponResponse createCoupon(CouponCreateRequest request);

    // 사용자에게 쿠폰 발급
    UserCouponResponse issueCoupon(UserCouponIssueRequest request);

    // Welcome 쿠폰 발급
    void issueWelcomCoupon(Long userId);

    // 쿠폰 사용 (주문 시)
    CouponApplyResponse applyCoupon(Long userCouponId, BigDecimal orderAmount);

    // 쿠폰 할인 계산
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount);

    // 사용자 쿠폰 목록 조회
    Page<UserCouponResponse> getUserCoupons(Long userId, Pageable pageable);

    //  사용 가능한 쿠폰 조회
    List<UserCouponResponse> getAvailableCoupons(Long userId);

    // 만료된 쿠폰 처리 (배치)
    void expireCoupons();
}
