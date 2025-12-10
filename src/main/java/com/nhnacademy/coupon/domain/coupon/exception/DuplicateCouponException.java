package com.nhnacademy.coupon.domain.coupon.exception;

/**
 * 쿠폰 중복 발급 시 발생하는 예외
 * - 동일한 쿠폰 정책으로 이미 발급받은 경우
 * - 카테고리/도서 쿠폰의 경우 같은 대상에 이미 발급받은 경우
 */

public class DuplicateCouponException extends RuntimeException {
    public DuplicateCouponException(String message) {
        super(message);
    }
}