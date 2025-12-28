package com.nhnacademy.coupon.domain.coupon.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 발급 요청을 위한 메시지 DTO (비동기 이벤트)")
public record CouponIssueMessage(
        Long userCreatedId
) {
}
