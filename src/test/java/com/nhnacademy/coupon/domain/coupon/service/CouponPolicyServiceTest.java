package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponCategory;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.repository.CouponBookRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponCategoryRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.impl.CouponPolicyServiceImpl;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CouponPolicyServiceTest {

    @Mock
    CouponPolicyRepository couponPolicyRepository;

    @Mock
    CouponCategoryRepository couponCategoryRepository;

    @Mock
    CouponBookRepository couponBookRepository;

    @Mock
    UserCouponRepository userCouponRepository;

    @InjectMocks
    CouponPolicyServiceImpl couponPolicyService;

    @Test
    @DisplayName("createCouponPolicy: CATEGORY면 coupon_categories 매핑 저장(saveAll)이 호출된다")
    void createCouponPolicy_category_saveMappings() {
        // given
        CouponPolicyCreateRequest req = spy(new CouponPolicyCreateRequest());
        req.setCouponPolicyName("카테고리 정책");
        req.setCouponType(CouponType.CATEGORY);
        req.setDiscountWay(DiscountWay.FIXED);
        req.setDiscountAmount(new BigDecimal("2000"));
        req.setValidDays(10);
        req.setCouponPolicyStatus(CouponPolicyStatus.ACTIVE);
        req.setCategoryIds(List.of(10L, 10L, 11L));

        CouponPolicy entity = req.toEntity();
        assertThat(entity).isNotNull();

        when(couponPolicyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        couponPolicyService.createCouponPolicy(req);

        // then
        verify(couponPolicyRepository, times(1)).save(any(CouponPolicy.class));

        verify(couponCategoryRepository, times(1)).saveAll(any());
        verify(couponBookRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("createCouponPolicy: BOOKS면 coupon_books 매핑 저장(saveAll)이 호출된다")
    void createCouponPolicy_books_saveMappings() {
        // given
        CouponPolicyCreateRequest req = spy(new CouponPolicyCreateRequest());
        req.setCouponPolicyName("도서 정책");
        req.setCouponType(CouponType.BOOKS);
        req.setDiscountWay(DiscountWay.FIXED);
        req.setDiscountAmount(new BigDecimal("2000"));
        req.setValidDays(10);
        req.setCouponPolicyStatus(CouponPolicyStatus.ACTIVE);
        req.setBookIds(List.of(100L, 200L, 200L));

        assertThat(req.toEntity()).isNotNull();

        when(couponPolicyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        couponPolicyService.createCouponPolicy(req);

        // then
        verify(couponPolicyRepository).save(any(CouponPolicy.class));
        verify(couponBookRepository).saveAll(any());
        verify(couponCategoryRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("createCouponPolicy: CATEGORY인데 categoryIds가 null이면 매핑 저장을 호출하지 않는다")
    void createCouponPolicy_category_nullIds_noSaveAll() {
        // given
        CouponPolicyCreateRequest req = spy(new CouponPolicyCreateRequest());
        req.setCouponPolicyName("카테고리 정책");
        req.setCouponType(CouponType.CATEGORY);
        req.setDiscountWay(DiscountWay.FIXED);
        req.setDiscountAmount(new BigDecimal("2000"));
        req.setValidDays(10);
        req.setCouponPolicyStatus(CouponPolicyStatus.ACTIVE);
        req.setCategoryIds(null);

        when(couponPolicyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        couponPolicyService.createCouponPolicy(req);

        // then
        verify(couponPolicyRepository).save(any());
        verify(couponBookRepository, never()).saveAll(any());
        verify(couponCategoryRepository, never()).saveAll(any());

    }
    @Test
    @DisplayName("createCouponPolicy: BOOKS인데 bookIds가 empty면 매핑 저장을 호출하지 않는다")
    void createCouponPolicy_books_emptyIds_noSaveAll() {
        // given
        CouponPolicyCreateRequest req = spy(new CouponPolicyCreateRequest());
        req.setCouponPolicyName("도서 정책");
        req.setCouponType(CouponType.BOOKS);
        req.setDiscountWay(DiscountWay.FIXED);
        req.setDiscountAmount(new BigDecimal("2000"));
        req.setValidDays(10);
        req.setCouponPolicyStatus(CouponPolicyStatus.ACTIVE);
        req.setBookIds(List.of());

        when(couponPolicyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        couponPolicyService.createCouponPolicy(req);

        // then
        verify(couponPolicyRepository).save(any());
        verify(couponBookRepository, never()).saveAll(any());
        verify(couponCategoryRepository, never()).saveAll(any());
    }
    @Test
    @DisplayName("createCouponPolicy: CATEGORY 매핑 저장 시 categoryIds 중복이 제거된다")
    void createCouponPolicy_category_distinct() {
        // given
        CouponPolicyCreateRequest req = spy(new CouponPolicyCreateRequest());
        req.setCouponPolicyName("카테고리 정책");
        req.setCouponType(CouponType.CATEGORY);
        req.setDiscountWay(DiscountWay.FIXED);
        req.setDiscountAmount(new BigDecimal("2000"));
        req.setValidDays(10);
        req.setCouponPolicyStatus(CouponPolicyStatus.ACTIVE);
        req.setCategoryIds(List.of(10L, 10L, 11L));

        when(couponPolicyRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        couponPolicyService.createCouponPolicy(req);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CouponCategory>> captor = ArgumentCaptor.forClass(List.class);
        verify(couponCategoryRepository).saveAll(captor.capture());

        List<CouponCategory> saved = captor.getValue();
        assertThat(saved)
                .extracting(CouponCategory::getCategoryId)
                .containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("updateCouponPolicy: 발급된 쿠폰이 있으면 상태만 변경한다")
    void updateCouponPolicy_whenIssued_thenOnlyStatusChanges() {
        // given
        Long policyId = 1L;

        CouponPolicy policy = CouponPolicy.builder()
                .couponPolicyId(policyId)
                .couponPolicyName("기존이름")
                .couponType(CouponType.CATEGORY)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(new BigDecimal("2000"))
                .validDays(10)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();

        when(couponPolicyRepository.findById(policyId)).thenReturn(java.util.Optional.of(policy));
        when(userCouponRepository.countByCouponPolicy_CouponPolicyId(policyId)).thenReturn(1L);
        when(couponPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // update request (mock)
        CouponPolicyUpdateRequest req = mock(CouponPolicyUpdateRequest.class);
        when(req.getPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);

        // when
        CouponPolicyResponse res = couponPolicyService.updateCouponPolicy(policyId, req);

        // then (상태만 변경)
        assertThat(policy.getCouponPolicyStatus()).isEqualTo(CouponPolicyStatus.DELETED);
        assertThat(policy.getCouponPolicyName()).isEqualTo("기존이름");
        assertThat(policy.getDiscountAmount()).isEqualByComparingTo("2000");

        verify(couponPolicyRepository).findById(policyId);
        verify(userCouponRepository).countByCouponPolicy_CouponPolicyId(policyId);
        verify(couponPolicyRepository).save(policy);
    }

    @Test
    @DisplayName("updateCouponPolicy: 정책이 존재하지 않으면 예외")
    void updateCouponPolicy_notFound_throws() {
        // given
        Long policyId = 999L;
        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        CouponPolicyUpdateRequest req = mock(CouponPolicyUpdateRequest.class);
        // when & then
        assertThatThrownBy(() -> couponPolicyService.updateCouponPolicy(policyId, req))
                .isInstanceOf(RuntimeException.class); // 실제 예외 타입으로 교체

        verify(couponPolicyRepository).findById(policyId);
    }



}