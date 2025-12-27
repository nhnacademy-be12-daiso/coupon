package com.nhnacademy.coupon.domain.coupon.dto.response;

import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategorySimpleResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.SingleCouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DtoTest {
    @Test
    @DisplayName("CategoryCouponResponse.of - CouponPolicy 필드가 policyInfo로 매핑된다")
    void categoryCouponResponse_of_mapsPolicyFields() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        CouponPolicy policy = CouponPolicy.builder()
                .couponPolicyId(10L)
                .couponPolicyName("테스트 정책")
                .couponType(CouponType.WELCOME)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(1000))
                .minOrderAmount(5000L)
                .maxDiscountAmount(10000L)
                .validDays(30)
                .validStartDate(start)
                .validEndDate(end)
                .quantity(100)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();

        Long categoryId = 400L;

        // when
        CategoryCouponResponse res = CategoryCouponResponse.of(policy, categoryId);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getBookCategoryId()).isEqualTo(categoryId);
        assertThat(res.getCouponCategoryName()).isNull(); // 현재는 null 고정

        CategoryCouponResponse.CouponPolicyInfo info = res.getPolicyInfo();
        assertThat(info).isNotNull();
        assertThat(info.getCouponPolicyId()).isEqualTo(10L);
        assertThat(info.getCouponPolicyName()).isEqualTo("테스트 정책");
        assertThat(info.getCouponType()).isEqualTo(CouponType.WELCOME);
        assertThat(info.getDiscountWay()).isEqualTo(DiscountWay.FIXED);
        assertThat(info.getDiscountAmount()).isEqualByComparingTo("1000");
        assertThat(info.getMinOrderAmount()).isEqualTo(5000L);
        assertThat(info.getMaxDiscountAmount()).isEqualTo(10000L);
        assertThat(info.getValidDays()).isEqualTo(30);
        assertThat(info.getValidStartDate()).isEqualTo(start);
        assertThat(info.getValidEndDate()).isEqualTo(end);
        assertThat(info.getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("CategorySimpleResponse - record 필드가 그대로 보존된다")
    void categorySimpleResponse_record() {
        CategorySimpleResponse res = new CategorySimpleResponse(1L, "자연과학");

        assertThat(res.categoryId()).isEqualTo(1L);
        assertThat(res.categoryName()).isEqualTo("자연과학");
    }

    @Test
    @DisplayName("SingleCouponApplyResponse - builder로 생성되고 값이 보존된다")
    void singleCouponApplyResponse_builder() {
        SingleCouponApplyResponse res = SingleCouponApplyResponse.builder()
                .bookId(100L)
                .userCouponId(200L)
                .couponName("생일 축하 쿠폰")
                .originalAmount(BigDecimal.valueOf(20000))
                .discountAmount(BigDecimal.valueOf(2000))
                .finalAmount(BigDecimal.valueOf(18000))
                .applicable(true)
                .message("적용 가능")
                .build();

        assertThat(res.bookId()).isEqualTo(100L);
        assertThat(res.userCouponId()).isEqualTo(200L);
        assertThat(res.couponName()).isEqualTo("생일 축하 쿠폰");
        assertThat(res.originalAmount()).isEqualByComparingTo("20000");
        assertThat(res.discountAmount()).isEqualByComparingTo("2000");
        assertThat(res.finalAmount()).isEqualByComparingTo("18000");
        assertThat(res.applicable()).isTrue();
        assertThat(res.message()).isEqualTo("적용 가능");
    }
}
