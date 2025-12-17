package com.nhnacademy.coupon.domain.coupon.exception;

public class CouponUpdateFailedException extends RuntimeException {
    public CouponUpdateFailedException(String message) {
        super(message);
    }
}
