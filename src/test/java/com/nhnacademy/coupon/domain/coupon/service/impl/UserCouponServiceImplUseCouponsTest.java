package com.nhnacademy.coupon.domain.coupon.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.exception.CouponUpdateFailedException;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

class UserCouponServiceImplUseCouponsTest {

    private UserCouponRepository userCouponRepository = mock(UserCouponRepository.class);
    private UserCouponServiceImpl userCouponService = new UserCouponServiceImpl(
            userCouponRepository,
            /* bookServiceClient */ null,
            /* couponPolicyRepository */ null,
            /* couponCategoryRepository */ null,
            /* couponBookRepository */ null
    );

    @Test
    @DisplayName("useCoupons - coupons null/empty면 return (repo 호출 없음)")
    void useCoupons_empty_returns() {
        userCouponService.useCoupons(1L, new BatchCouponUseRequest(100L, null));
        userCouponService.useCoupons(1L, new BatchCouponUseRequest(100L, List.of()));

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("useCoupons - 한 주문에서 동일 쿠폰 중복이면 CouponUpdateFailedException")
    void useCoupons_duplicateIds_throwsUpdateFailed() {
        Long userId = 1L;

        BatchCouponUseRequest req = new BatchCouponUseRequest(
                100L,
                List.of(new CouponUseRequest(1L), new CouponUseRequest(1L))
        );

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("동일 쿠폰");

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("useCoupons - 멱등: USED && usedOrderId 동일이면 continue (coupon.use 호출 안 함)")
    void useCoupons_idempotent_usedSameOrder_continues() {
        Long userId = 1L;
        Long orderId = 100L;
        Long couponId = 1L;

        BatchCouponUseRequest req = new BatchCouponUseRequest(orderId, List.of(new CouponUseRequest(couponId)));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(couponId);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.USED);
        when(coupon.getUsedOrderId()).thenReturn(orderId);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(couponId))).thenReturn(List.of(coupon));

        assertThatCode(() -> userCouponService.useCoupons(userId, req)).doesNotThrowAnyException();

        verify(coupon, never()).use(anyLong());
    }

    @Test
    @DisplayName("useCoupons - USED지만 usedOrderId 다르면 status 검증에서 실패(CouponUpdateFailedException)")
    void useCoupons_usedDifferentOrder_throwsUpdateFailed() {
        Long userId = 1L;
        Long orderId = 100L;
        Long couponId = 1L;

        BatchCouponUseRequest req = new BatchCouponUseRequest(orderId, List.of(new CouponUseRequest(couponId)));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(couponId);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.USED);
        when(coupon.getUsedOrderId()).thenReturn(999L); // 다른 주문
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(couponId))).thenReturn(List.of(coupon));

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("사용할 수 없는 쿠폰");

        verify(coupon, never()).use(anyLong());
    }
    @Test
    @DisplayName("useCoupons - 정상 케이스: ISSUED & 미만료면 coupon.use(orderId) 호출")
    void useCoupons_success_callsUse() {
        Long userId = 1L, orderId = 100L, couponId = 1L;

        BatchCouponUseRequest req = new BatchCouponUseRequest(orderId, List.of(new CouponUseRequest(couponId)));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(couponId);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(couponId))).thenReturn(List.of(coupon));

        userCouponService.useCoupons(userId, req);

        verify(coupon).use(orderId);
    }


    @Test
    @DisplayName("useCoupons - 소유자 불일치면 CouponUpdateFailedException")
    void useCoupons_notOwner_throwsUpdateFailed() {
        Long userId = 1L;
        Long orderId = 100L;
        Long couponId = 1L;

        BatchCouponUseRequest req =
                new BatchCouponUseRequest(orderId, List.of(new CouponUseRequest(couponId)));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(couponId);
        when(coupon.getUserId()).thenReturn(999L); // ⭐ 다른 유저
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(couponId))).thenReturn(List.of(coupon));

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("본인의 쿠폰");

        verify(coupon, never()).use(anyLong());
    }

    @Test
    @DisplayName("useCoupons - 만료 쿠폰이면 CouponUpdateFailedException")
    void useCoupons_expired_throwsUpdateFailed() {
        Long userId = 1L;
        Long orderId = 100L;
        Long couponId = 1L;

        BatchCouponUseRequest req =
                new BatchCouponUseRequest(orderId, List.of(new CouponUseRequest(couponId)));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(couponId);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().minusSeconds(1)); // ⭐ 만료

        when(userCouponRepository.findAllById(List.of(couponId))).thenReturn(List.of(coupon));

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("만료된 쿠폰");

        verify(coupon, never()).use(anyLong());
    }


}
