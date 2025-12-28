package com.nhnacademy.coupon.domain.coupon.entity;

import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class CouponPolicyEntityTest {

    private CouponPolicy base(Integer quantity) {
        return CouponPolicy.builder()
                .couponPolicyId(1L)
                .couponPolicyName("테스트")
                .couponType(CouponType.WELCOME)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(1000))
                .quantity(quantity)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("decreaseQuantity - quantity null이면 무제한으로 통과")
    void decreaseQuantity_unlimited() {
        CouponPolicy policy = base(null);

        policy.decreaseQuantity(); // 예외 없어야 함

        assertThat(policy.getQuantity()).isNull();
    }

    @Test
    @DisplayName("decreaseQuantity - quantity > 0 이면 1 감소")
    void decreaseQuantity_success() {
        CouponPolicy policy = base(2);

        policy.decreaseQuantity();

        assertThat(policy.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("decreaseQuantity - quantity 0이면 예외")
    void decreaseQuantity_zero_throws() {
        CouponPolicy policy = base(0);

        assertThatThrownBy(policy::decreaseQuantity)
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("발급 가능한 쿠폰이 없습니다");
    }

    @Test
    @DisplayName("increaseQuantity - quantity가 있으면 증가")
    void increaseQuantity_success() {
        CouponPolicy policy = base(1);

        policy.increaseQuantity();

        assertThat(policy.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("increaseQuantity - quantity null이면 변화 없음")
    void increaseQuantity_unlimited_noChange() {
        CouponPolicy policy = base(null);

        policy.increaseQuantity();

        assertThat(policy.getQuantity()).isNull();
    }

    @Test
    @DisplayName("updateStatus - 상태만 변경된다")
    void updateStatus_success() {
        CouponPolicy policy = base(1);

        policy.updateStatus(CouponPolicyStatus.DELETED);

        assertThat(policy.getCouponPolicyStatus()).isEqualTo(CouponPolicyStatus.DELETED);
    }
}
