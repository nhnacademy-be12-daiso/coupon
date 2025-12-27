package com.nhnacademy.coupon.domain.coupon.entity;

import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class UserCouponTest {

    private UserCoupon base(CouponStatus status, LocalDateTime expiryAt) {
        return UserCoupon.builder()
                .userCouponId(1L)
                .couponPolicy(null) // use/cancel/isAvailable에 필요 없음
                .userId(100L)
                .status(status)
                .issuedAt(LocalDateTime.now().minusDays(1))
                .expiryAt(expiryAt)
                .usedAt(null)
                .usedOrderId(null)
                .build();
    }

    @Test
    @DisplayName("use - ISSUED면 USED로 변경되고 usedAt/usedOrderId가 세팅된다")
    void use_success_fromIssued() {
        UserCoupon coupon = base(CouponStatus.ISSUED, LocalDateTime.now().plusDays(1));

        coupon.use(777L);

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
        assertThat(coupon.getUsedOrderId()).isEqualTo(777L);
        assertThat(coupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("use - CANCELED도 사용 가능")
    void use_success_fromCanceled() {
        UserCoupon coupon = base(CouponStatus.CANCELED, LocalDateTime.now().plusDays(1));

        coupon.use(888L);

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
        assertThat(coupon.getUsedOrderId()).isEqualTo(888L);
        assertThat(coupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("use - ISSUED/CANCELED가 아니면 예외")
    void use_invalidStatus_throws() {
        UserCoupon coupon = base(CouponStatus.USED, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> coupon.use(123L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("쿠폰을 사용할 수 없는 상태");
    }

    @Test
    @DisplayName("use - 만료되었으면 예외")
    void use_expired_throws() {
        UserCoupon coupon = base(CouponStatus.ISSUED, LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> coupon.use(123L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("만료된 쿠폰");
    }

    @Test
    @DisplayName("expire - ISSUED면 EXPIRED로 변경")
    void expire_onlyIssued_changesToExpired() {
        UserCoupon coupon = base(CouponStatus.ISSUED, LocalDateTime.now().plusDays(1));

        coupon.expire();

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }

    @Test
    @DisplayName("expire - ISSUED가 아니면 변화 없음")
    void expire_notIssued_noChange() {
        UserCoupon coupon = base(CouponStatus.USED, LocalDateTime.now().plusDays(1));

        coupon.expire();

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    @DisplayName("cancel - USED 상태면 ISSUED로 복구되고 usedAt/usedOrderId가 null로 초기화")
    void cancel_success_fromUsed() {
        UserCoupon coupon = base(CouponStatus.USED, LocalDateTime.now().plusDays(1));
        // usedAt/usedOrderId를 세팅해 둠
        UserCoupon seeded = UserCoupon.builder()
                .userCouponId(coupon.getUserCouponId())
                .couponPolicy(null)
                .userId(coupon.getUserId())
                .status(CouponStatus.USED)
                .issuedAt(coupon.getIssuedAt())
                .expiryAt(coupon.getExpiryAt())
                .usedAt(LocalDateTime.now().minusHours(1))
                .usedOrderId(555L)
                .build();

        seeded.cancel(555L);

        assertThat(seeded.getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(seeded.getUsedAt()).isNull();
        assertThat(seeded.getUsedOrderId()).isNull();
    }

    @Test
    @DisplayName("cancel - USED가 아니면 예외")
    void cancel_notUsed_throws() {
        UserCoupon coupon = base(CouponStatus.ISSUED, LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> coupon.cancel(1L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("사용된 쿠폰만 취소");
    }

    @Test
    @DisplayName("cancel - 만료된 쿠폰은 복구 불가")
    void cancel_expired_throws() {
        UserCoupon coupon = base(CouponStatus.USED, LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> coupon.cancel(1L))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("이미 만료된 쿠폰은 복구");
    }

    @Test
    @DisplayName("isAvailable - ISSUED/CANCELED이고 만료 전이면 true")
    void isAvailable_true_whenIssuedOrCanceledAndNotExpired() {
        assertThat(base(CouponStatus.ISSUED, LocalDateTime.now().plusDays(1)).isAvailable()).isTrue();
        assertThat(base(CouponStatus.CANCELED, LocalDateTime.now().plusDays(1)).isAvailable()).isTrue();
    }

    @Test
    @DisplayName("isAvailable - 만료거나 상태가 다르면 false")
    void isAvailable_false_whenExpiredOrInvalidStatus() {
        assertThat(base(CouponStatus.ISSUED, LocalDateTime.now().minusSeconds(1)).isAvailable()).isFalse();
        assertThat(base(CouponStatus.USED, LocalDateTime.now().plusDays(1)).isAvailable()).isFalse();
        assertThat(base(CouponStatus.EXPIRED, LocalDateTime.now().plusDays(1)).isAvailable()).isFalse();
    }
}
