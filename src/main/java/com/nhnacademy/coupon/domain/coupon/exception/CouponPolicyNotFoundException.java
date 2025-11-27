package com.nhnacademy.coupon.domain.coupon.exception;

// 런타임에 발생하는 언체크 예외로 정의
public class CouponPolicyNotFoundException extends RuntimeException {

    public CouponPolicyNotFoundException() {
        super("쿠폰을 찾을 수 없습니다.");
    }

    public CouponPolicyNotFoundException(String message) {
        super(message);
    }

    public CouponPolicyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}