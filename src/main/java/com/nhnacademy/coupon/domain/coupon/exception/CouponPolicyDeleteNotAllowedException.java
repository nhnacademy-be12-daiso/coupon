package com.nhnacademy.coupon.domain.coupon.exception;

public class CouponPolicyDeleteNotAllowedException extends RuntimeException {
    public CouponPolicyDeleteNotAllowedException(String message) {
        super(message);
    }
}
