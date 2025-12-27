package com.nhnacademy.coupon.domain.coupon.service.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

class UserCouponServiceImplQuantityTest {

    @Mock private CouponPolicyRepository couponPolicyRepository;
    @Mock private UserCouponRepository userCouponRepository;

    @InjectMocks private UserCouponServiceImpl userCouponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private CouponPolicy basePolicy(Long policyId, Integer quantity) {
        return CouponPolicy.builder()
                .couponPolicyId(policyId)
                .couponPolicyName("테스트")
                .couponType(CouponType.WELCOME)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(1000))
                .minOrderAmount(null)
                .maxDiscountAmount(null)
                .validDays(30)
                .validStartDate(null)
                .validEndDate(null)
                .quantity(quantity) // null / 0 / 1 제어
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("downloadCoupon - quantity가 null(무제한)이면 decreaseQuantity는 통과하고 저장된다")
    void downloadCoupon_quantityNull_unlimited_success() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = basePolicy(policyId, null);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId))
                .thenReturn(false);

        when(userCouponRepository.save(any(UserCoupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // 저장한 객체 그대로 반환

        UserCouponResponse response = userCouponService.downloadCoupon(userId, policyId);

        assertThat(response).isNotNull();
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("downloadCoupon - quantity가 0이면 InvalidCouponException, save 호출 안 됨")
    void downloadCoupon_quantityZero_throws_andNotSave() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = basePolicy(policyId, 0);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId))
                .thenReturn(false);

        assertThatThrownBy(() -> userCouponService.downloadCoupon(userId, policyId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("발급 가능한 쿠폰이 없습니다");

        verify(userCouponRepository, never()).save(any());
    }

    @Test
    @DisplayName("downloadCoupon - quantity가 1이면 성공 후 quantity가 0으로 감소한다")
    void downloadCoupon_quantityOne_decreasesToZero() {
        Long userId = 1L;
        Long policyId = 10L;

        CouponPolicy policy = basePolicy(policyId, 1);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policyId))
                .thenReturn(false);

        // save 결과는 from(saved) 때문에 최소 필드 필요 -> 저장한 객체 그대로 반환
        when(userCouponRepository.save(any(UserCoupon.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserCouponResponse response = userCouponService.downloadCoupon(userId, policyId);

        assertThat(response).isNotNull();
        assertThat(policy.getQuantity()).isZero();          // ⭐ 핵심: 실제로 감소했는지
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

}
