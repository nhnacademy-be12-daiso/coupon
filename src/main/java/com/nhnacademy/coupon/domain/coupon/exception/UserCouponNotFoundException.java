package com.nhnacademy.coupon.domain.coupon.exception;

public class UserCouponNotFoundException extends RuntimeException {
    public UserCouponNotFoundException(Long userCouponId) {
        super("유저 쿠폰을 찾을 수 없습니다. userCouponId=" + userCouponId);
    }
}
