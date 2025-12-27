package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.CouponUpdateFailedException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponBookRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponCategoryRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserCouponServiceImplTest {

    @Mock private UserCouponRepository userCouponRepository;
    @Mock private BookServiceClient bookServiceClient;
    @Mock private CouponPolicyRepository couponPolicyRepository;
    @Mock private CouponCategoryRepository couponCategoryRepository;
    @Mock private CouponBookRepository couponBookRepository;

    @InjectMocks private UserCouponServiceImpl userCouponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================
    // downloadCoupon() 테스트
    // =========================

    @Test
    @DisplayName("downloadCoupon - 정책이 없으면 CouponPolicyNotFoundException")
    void downloadCoupon_policyNotFound_throws() {
        Long userId = 1L;
        Long policyId = 10L;

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(CouponPolicyNotFoundException.class);

        verify(couponPolicyRepository).findById(policyId);
        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("downloadCoupon - 정책 상태가 ACTIVE 아니면 InvalidCouponException")
    void downloadCoupon_policyNotActive_throws() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);
        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("발급 불가능");

        verify(couponPolicyRepository).findById(policyId);
        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("downloadCoupon - 발급 시작 전이면 InvalidCouponException")
    void downloadCoupon_beforeStart_throws() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidStartDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(policy.getValidEndDate()).thenReturn(null);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("발급 기간");

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("downloadCoupon - 발급 종료 후이면 InvalidCouponException")
    void downloadCoupon_afterEnd_throws() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidStartDate()).thenReturn(null);
        when(policy.getValidEndDate()).thenReturn(LocalDateTime.now().minusSeconds(1));

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("지났습니다");

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("downloadCoupon - 이미 다운로드한 쿠폰이면 InvalidCouponException")
    void downloadCoupon_alreadyDownloaded_throws() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidStartDate()).thenReturn(null);
        when(policy.getValidEndDate()).thenReturn(null);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId))
                .thenReturn(true);

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("이미 다운로드");

        verify(userCouponRepository).existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId);
        verify(userCouponRepository, never()).save(any());
    }

    @Test
    @DisplayName("downloadCoupon - validDays가 있으면 expiryAt=now+validDays, 저장 후 응답 반환")
    void downloadCoupon_success_withValidDays() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidStartDate()).thenReturn(null);
        when(policy.getValidEndDate()).thenReturn(null);
        when(policy.getValidDays()).thenReturn(30);
        when(policy.getCouponPolicyId()).thenReturn(policyId); // from()에서 쓸 가능성 높음

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId))
                .thenReturn(false);

        // save 결과 mock에 "from()"이 접근하는 값들만 채워주기
        UserCoupon saved = mock(UserCoupon.class);
        when(saved.getCouponPolicy()).thenReturn(policy);       //  NPE 방지 핵심
        when(saved.getUserCouponId()).thenReturn(999L);         // 필요하면
        when(saved.getUserId()).thenReturn(userId);             // 필요하면
        when(saved.getStatus()).thenReturn(CouponStatus.ISSUED);// 필요하면
        when(saved.getIssuedAt()).thenReturn(LocalDateTime.now());// 필요하면
        when(saved.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(30)); // 필요하면

        when(userCouponRepository.save(any(UserCoupon.class))).thenReturn(saved);

        assertThatCode(() -> userCouponService.downloadCoupon(userId, policyId))
                .doesNotThrowAnyException();

        verify(policy).decreaseQuantity();
        verify(userCouponRepository).save(any(UserCoupon.class));
    }


    // =========================
    // calculateDiscount() 테스트
    // =========================

    @Test
    @DisplayName("calculateDiscount - minOrderAmount 미달이면 0")
    void calculateDiscount_minOrderNotMet_returnsZero() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getMinOrderAmount()).thenReturn(50_000L);

        BigDecimal discount = userCouponService.calculateDiscount(policy, BigDecimal.valueOf(49_999));

        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateDiscount - FIXED 할인")
    void calculateDiscount_fixed() {
        CouponPolicy policy = mock(CouponPolicy.class);
        System.out.println("max=" + policy.getMaxDiscountAmount());

        when(policy.getMinOrderAmount()).thenReturn(null);
        when(policy.getDiscountWay()).thenReturn(DiscountWay.FIXED);
        when(policy.getDiscountAmount()).thenReturn(BigDecimal.valueOf(10_000));
        when(policy.getMaxDiscountAmount()).thenReturn(null);

        BigDecimal discount = userCouponService.calculateDiscount(policy, BigDecimal.valueOf(100_000));

        assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(10_000));
    }

    @Test
    @DisplayName("calculateDiscount - PERCENT 할인 + maxDiscount cap 적용")
    void calculateDiscount_percent_withMaxCap() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getMinOrderAmount()).thenReturn(null);
        when(policy.getDiscountWay()).thenReturn(DiscountWay.PERCENT);
        when(policy.getDiscountAmount()).thenReturn(BigDecimal.valueOf(20)); // 20%
        when(policy.getMaxDiscountAmount()).thenReturn(5_000L); // cap 5,000

        BigDecimal discount = userCouponService.calculateDiscount(policy, BigDecimal.valueOf(100_000)); // 20,000 -> cap 5,000

        assertThat(discount).isEqualByComparingTo(BigDecimal.valueOf(5_000));
    }

    // =========================
    // useCoupons() 테스트
    // =========================

    @Test
    @DisplayName("useCoupons - coupons가 null/empty면 정상 return")
    void useCoupons_empty_ok() {
        userCouponService.useCoupons(1L, new BatchCouponUseRequest(100L, null));
        userCouponService.useCoupons(1L, new BatchCouponUseRequest(100L, List.of()));

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("useCoupons - 한 주문에서 동일 쿠폰 중복이면 CouponUpdateFailedException")
    void useCoupons_duplicateCouponId_throwsUpdateFailed() {
        Long userId = 1L;

        var req = new BatchCouponUseRequest(
                100L,
                List.of(
                        new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L),
                        new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L)
                )
        );

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("동일 쿠폰");

        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("useCoupons - 쿠폰 일부 조회 실패(size 불일치)면 CouponUpdateFailedException")
    void useCoupons_someNotFound_throwsUpdateFailed() {
        Long userId = 1L;

        var req = new BatchCouponUseRequest(
                100L,
                List.of(
                        new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L),
                        new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(2L)
                )
        );

        when(userCouponRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(mock(UserCoupon.class))); // 일부만 반환 -> size mismatch

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class);

        verify(userCouponRepository).findAllById(List.of(1L, 2L));
    }

    @Test
    @DisplayName("useCoupons - 멱등: 이미 USED && usedOrderId가 동일하면 continue (예외 없이 통과)")
    void useCoupons_idempotent_usedSameOrder_ok() {
        Long userId = 1L;
        Long orderId = 100L;

        var req = new BatchCouponUseRequest(
                orderId,
                List.of(new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L))
        );

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(1L);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.USED);
        when(coupon.getUsedOrderId()).thenReturn(orderId);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(1L))).thenReturn(List.of(coupon));

        assertThatCode(() -> userCouponService.useCoupons(userId, req))
                .doesNotThrowAnyException();

        // 멱등 분기는 use() 호출이 없어야 함
        verify(coupon, never()).use(anyLong());
    }

    @Test
    @DisplayName("useCoupons - 소유자가 아니면 CouponUpdateFailedException")
    void useCoupons_notOwner_throwsUpdateFailed() {
        Long userId = 1L;
        Long orderId = 100L;

        var req = new BatchCouponUseRequest(
                orderId,
                List.of(new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L))
        );

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(1L);
        when(coupon.getUserId()).thenReturn(999L); // 다른 유저
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(1L))).thenReturn(List.of(coupon));

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

        var req = new BatchCouponUseRequest(
                orderId,
                List.of(new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L))
        );

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(1L);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().minusSeconds(1)); // 만료

        when(userCouponRepository.findAllById(List.of(1L))).thenReturn(List.of(coupon));

        assertThatThrownBy(() -> userCouponService.useCoupons(userId, req))
                .isInstanceOf(CouponUpdateFailedException.class)
                .hasMessageContaining("만료");

        verify(coupon, never()).use(anyLong());
    }

    @Test
    @DisplayName("useCoupons - 정상 사용이면 use(orderId) 호출")
    void useCoupons_success_callsUse() {
        Long userId = 1L;
        Long orderId = 100L;

        var req = new BatchCouponUseRequest(
                orderId,
                List.of(new com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest(1L))
        );

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getUserCouponId()).thenReturn(1L);
        when(coupon.getUserId()).thenReturn(userId);
        when(coupon.getStatus()).thenReturn(CouponStatus.ISSUED);
        when(coupon.getExpiryAt()).thenReturn(LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findAllById(List.of(1L))).thenReturn(List.of(coupon));

        assertThatCode(() -> userCouponService.useCoupons(userId, req))
                .doesNotThrowAnyException();

        verify(coupon).use(orderId);
    }
}
