package com.nhnacademy.coupon.domain.coupon.dto.query;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookCouponQuery {
    private final Long userId;
    private final Long bookId;
    private final Long primaryCategoryId;
    private final Long secondaryCategoryId;
}
