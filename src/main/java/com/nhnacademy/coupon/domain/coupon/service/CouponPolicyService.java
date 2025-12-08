package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import java.util.List;

public interface CouponPolicyService {
    // 쿠폰 정책 생성
    CouponPolicyResponse createCouponPolicy(CouponPolicyCreateRequest request);

    // 쿠폰 정책 전체 조회
    List<CouponPolicyResponse> couponPolices();

    // 쿠폰 정책 단일 조회
    CouponPolicyResponse couponPolicyDetail(Long id);

    // 수정 (발급 전에만 가능)
    CouponPolicyResponse updateCouponPolicy(Long id, CouponPolicyUpdateRequest request);

    // 사용자에게 쿠폰 발급
    UserCouponResponse issueCoupon(Long userId, UserCouponIssueRequest request);

    // Welcome 쿠폰 발급
    void issueWelcomeCoupon(Long userId);


}
