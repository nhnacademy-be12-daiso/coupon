package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.query.BookCouponQuery;
import com.nhnacademy.coupon.domain.coupon.dto.request.issue.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponCategory;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyDeleteNotAllowedException;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.*;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
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

    @Mock
    UserCouponJdbcRepository userCouponJdbcRepository;

    @Spy
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

    @Test
    @DisplayName("issueWelcomeCoupon - Welcome 정책이 없으면 warn 로그만 출력")
    void issueWelcomeCoupon_noPolicies_warnsOnly() {
        when(couponPolicyRepository.findByCouponType(CouponType.WELCOME))
                .thenReturn(List.of());

        assertThatCode(() -> couponPolicyService.issueWelcomeCoupon(1L))
                .doesNotThrowAnyException();

        verify(userCouponRepository, never()).save(any());
    }

    @Test
    @DisplayName("issueWelcomeCoupon - 이미 발급된 경우 스킵")
    void issueWelcomeCoupon_alreadyIssued_skips() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyId()).thenReturn(1L);

        when(couponPolicyRepository.findByCouponType(CouponType.WELCOME))
                .thenReturn(List.of(policy));
        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(1L, 1L))
                .thenReturn(true);

        couponPolicyService.issueWelcomeCoupon(1L);

        verify(userCouponRepository, never()).save(any());
    }

    @Test
    @DisplayName("issueWelcomeCoupon 발급 중 예외 발생해도 회원가입은 정상 처리")
    void issueWelcomeCoupon_exceptionDuringIssue_continues() {
        // given
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyId()).thenReturn(1L);

        when(couponPolicyRepository.findByCouponType(CouponType.WELCOME))
                .thenReturn(List.of(policy));

        when(userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(1L, 1L))
                .thenReturn(false);

        // 핵심: issueWelcomeCoupon 내부에서 호출되는 issueCoupon 강제 예외 \-\> catch 분기 커버
        doThrow(new RuntimeException("forced"))
                .when(couponPolicyService)
                .issueCoupon(eq(1L), any(UserCouponIssueRequest.class));

        // when \& then
        assertThatCode(() -> couponPolicyService.issueWelcomeCoupon(1L))
                .doesNotThrowAnyException();

        // 분기 실제로 탔는지 보증
        verify(couponPolicyService, times(1))
                .issueCoupon(eq(1L), any(UserCouponIssueRequest.class));
    }

    @Test
    @DisplayName("issueBirthdayCouponsBulk - 대량 발급 성공")
    void issueBirthdayCouponsBulk_success() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyId()).thenReturn(1L);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidDays()).thenReturn(7);

        when(couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY))
                .thenReturn(List.of(policy));
        when(userCouponJdbcRepository.batchInsertBirthdayCoupons(any(), any(), any(), any()))
                .thenReturn(new int[]{1, 1, 1});

        long result = couponPolicyService.issueBirthdayCouponsBulk(List.of(1L, 2L, 3L));

        assertThat(result).isEqualTo(3);
        verify(userCouponJdbcRepository).batchInsertBirthdayCoupons(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getAvailableCouponsForBook - 카테고리 정보 없으면 빈 리스트")
    void getAvailableCouponsForBook_noCategories_empty() {
        BookCouponQuery query = BookCouponQuery.builder()
                .userId(1L)
                .bookId(100L)
                .primaryCategoryId(null)
                .secondaryCategoryId(null)
                .build();

        when(couponPolicyRepository.findAllAvailable(any(), any()))
                .thenReturn(List.of());

        List<CategoryCouponResponse> result =
                couponPolicyService.getAvailableCouponsForBook(query);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("couponPolices - 전체 쿠폰 정책 조회")
    void couponPolices_returnsAll() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyId()).thenReturn(1L);
        when(policy.getCouponPolicyName()).thenReturn("테스트 정책");

        when(couponPolicyRepository.findAll()).thenReturn(List.of(policy));

        var result = couponPolicyService.couponPolices();

        assertThat(result).hasSize(1);
        verify(couponPolicyRepository).findAll();
    }

    @Test
    @DisplayName("couponPolices - 빈 목록이면 빈 리스트 반환")
    void couponPolices_empty() {
        when(couponPolicyRepository.findAll()).thenReturn(List.of());

        var result = couponPolicyService.couponPolices();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("couponPolicyDetail - 정책 단일 조회 성공")
    void couponPolicyDetail_found() {
        Long policyId = 1L;
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyId()).thenReturn(policyId);
        when(policy.getCouponPolicyName()).thenReturn("테스트 정책");

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        var result = couponPolicyService.couponPolicyDetail(policyId);

        assertThat(result).isNotNull();
        verify(couponPolicyRepository).findById(policyId);
    }

    @Test
    @DisplayName("couponPolicyDetail - 정책 없으면 예외")
    void couponPolicyDetail_notFound() {
        Long policyId = 999L;
        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponPolicyService.couponPolicyDetail(policyId))
                .isInstanceOf(RuntimeException.class);

        verify(couponPolicyRepository).findById(policyId);
    }

    @Test
    @DisplayName("issueBirthdayCouponsBulk - userIds가 비어있고 ACTIVE 생일 정책이 없으면 예외")
    void issueBirthdayCouponsBulk_emptyUsers_noActivePolicy_throws() {
        // given
        CouponPolicy inactive = mock(CouponPolicy.class);
        when(inactive.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);

        when(couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY))
                .thenReturn(List.of(inactive));

        // when & then
        assertThatThrownBy(() -> couponPolicyService.issueBirthdayCouponsBulk(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("생일 쿠폰 정책");

        verify(userCouponJdbcRepository, never()).batchInsertBirthdayCoupons(any(), any(), any(), any());
    }

    @Test
    @DisplayName("issueBirthdayCouponsBulk - userIds가 비어있어도 ACTIVE 생일 정책이 있으면 0 반환")
    void issueBirthdayCouponsBulk_emptyUsers_withActivePolicy_returns0() {
        // given
        CouponPolicy active = mock(CouponPolicy.class);
        when(active.getCouponPolicyId()).thenReturn(1L);
        when(active.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(active.getValidDays()).thenReturn(7);

        when(couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY))
                .thenReturn(List.of(active));

        when(userCouponJdbcRepository.batchInsertBirthdayCoupons(any(), any(), any(), any()))
                .thenReturn(new int[]{}); // 0건

        // when
        long result = couponPolicyService.issueBirthdayCouponsBulk(List.of());

        // then
        org.assertj.core.api.Assertions.assertThat(result).isEqualTo(0);
        verify(userCouponJdbcRepository, times(1)).batchInsertBirthdayCoupons(any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateCouponPolicy: 발급된 쿠폰이 없으면 요청값으로 전체 필드를 변경한다")
    void updateCouponPolicy_whenNotIssued_thenUpdatesFields() {
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

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.countByCouponPolicy_CouponPolicyId(policyId)).thenReturn(0L);
        when(couponPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CouponPolicyUpdateRequest req = mock(CouponPolicyUpdateRequest.class);
        when(req.getCouponPolicyName()).thenReturn("변경이름");
        when(req.getDiscountWay()).thenReturn(DiscountWay.PERCENT);
        when(req.getDiscountAmount()).thenReturn(new BigDecimal("10"));
        when(req.getValidDays()).thenReturn(30);
        when(req.getPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);

        // when
        var res = couponPolicyService.updateCouponPolicy(policyId, req);

        // then
        assertThat(res).isNotNull();
        assertThat(policy.getCouponPolicyName()).isEqualTo("변경이름");
        assertThat(policy.getDiscountWay()).isEqualTo(DiscountWay.PERCENT);
        assertThat(policy.getDiscountAmount()).isEqualByComparingTo("10");
        assertThat(policy.getValidDays()).isEqualTo(30);
        assertThat(policy.getCouponPolicyStatus()).isEqualTo(CouponPolicyStatus.DELETED);

        verify(couponPolicyRepository).findById(policyId);
        verify(userCouponRepository).countByCouponPolicy_CouponPolicyId(policyId);
        verify(couponPolicyRepository).save(policy);
    }

    @Test
    @DisplayName("issueBirthdayCouponsBulk: ACTIVE 정책은 있으나 insert 결과가 0이면 0을 반환한다")
    void issueBirthdayCouponsBulk_activePolicy_butNoInserted_returns0() {
        // given
        CouponPolicy active = mock(CouponPolicy.class);
        when(active.getCouponPolicyId()).thenReturn(1L);
        when(active.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(active.getValidDays()).thenReturn(7);

        when(couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY))
                .thenReturn(List.of(active));

        when(userCouponJdbcRepository.batchInsertBirthdayCoupons(any(), any(), any(), any()))
                .thenReturn(new int[]{0, 0, 0});

        // when
        long result = couponPolicyService.issueBirthdayCouponsBulk(List.of(1L, 2L, 3L));

        // then
        assertThat(result).isEqualTo(0);
        verify(userCouponJdbcRepository, times(1)).batchInsertBirthdayCoupons(any(), any(), any(), any());
    }

    @Test
    @DisplayName("issueBirthdayCouponsBulk: JDBC insert 중 예외가 발생하면 예외를 전파한다")
    void issueBirthdayCouponsBulk_jdbcThrows_propagates() {
        // given
        CouponPolicy active = mock(CouponPolicy.class);
        when(active.getCouponPolicyId()).thenReturn(1L);
        when(active.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(active.getValidDays()).thenReturn(7);

        when(couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY))
                .thenReturn(List.of(active));

        when(userCouponJdbcRepository.batchInsertBirthdayCoupons(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("db error"));

        // when & then
        assertThatThrownBy(() -> couponPolicyService.issueBirthdayCouponsBulk(List.of(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");

        verify(userCouponJdbcRepository, times(1)).batchInsertBirthdayCoupons(any(), any(), any(), any());
    }


    @Test
    @DisplayName("updateCouponPolicy: 발급된 쿠폰이 있으면 상태만 변경하고 저장한다")
    void updateCouponPolicy_issued_thenSavesWithStatusOnly() {
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

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.countByCouponPolicy_CouponPolicyId(policyId)).thenReturn(5L);
        when(couponPolicyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CouponPolicyUpdateRequest req = mock(CouponPolicyUpdateRequest.class);
        when(req.getPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);

        var res = couponPolicyService.updateCouponPolicy(policyId, req);

        assertThat(res).isNotNull();
        assertThat(policy.getCouponPolicyStatus()).isEqualTo(CouponPolicyStatus.DELETED);
        assertThat(policy.getCouponPolicyName()).isEqualTo("기존이름");

        verify(couponPolicyRepository, times(1)).findById(policyId);
        verify(userCouponRepository, times(1)).countByCouponPolicy_CouponPolicyId(policyId);
        verify(couponPolicyRepository, times(1)).save(policy);
    }

    @Test
    @DisplayName("issueCoupon - 비활성 정책이면 예외")
    void issueCoupon_inactivePolicy_throws() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.DELETED);

        when(couponPolicyRepository.findById(1L))
                .thenReturn(Optional.of(policy));

        UserCouponIssueRequest req = new UserCouponIssueRequest(1L);

        assertThatThrownBy(() -> couponPolicyService.issueCoupon(1L, req))
                .isInstanceOf(InvalidCouponException.class);
    }

    @Test
    @DisplayName("issueCoupon - 발급 시작일 이전이면 예외")
    void issueCoupon_beforeStartDate_throws() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidStartDate()).thenReturn(LocalDateTime.now().plusDays(1));

        when(couponPolicyRepository.findById(1L))
                .thenReturn(Optional.of(policy));

        UserCouponIssueRequest req = new UserCouponIssueRequest(1L);

        assertThatThrownBy(() -> couponPolicyService.issueCoupon(1L, req))
                .isInstanceOf(InvalidCouponException.class);
    }

    @Test
    @DisplayName("issueCoupon - 발급 종료일 이후면 예외")
    void issueCoupon_afterEndDate_throws() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponPolicyStatus()).thenReturn(CouponPolicyStatus.ACTIVE);
        when(policy.getValidEndDate()).thenReturn(LocalDateTime.now().minusDays(1));

        when(couponPolicyRepository.findById(1L))
                .thenReturn(Optional.of(policy));

        UserCouponIssueRequest req = new UserCouponIssueRequest(1L);

        assertThatThrownBy(() -> couponPolicyService.issueCoupon(1L, req))
                .isInstanceOf(InvalidCouponException.class);
    }

    @Test
    @DisplayName("getAvailableCouponsForBook - GENERAL 타입은 필터링되어 제외")
    void getAvailableCouponsForBook_generalType_excluded() {
        CouponPolicy policy = mock(CouponPolicy.class);
        when(policy.getCouponType()).thenReturn(CouponType.GENERAL);
        when(policy.getCouponPolicyId()).thenReturn(1L);
        when(policy.getQuantity()).thenReturn(10);

        when(couponPolicyRepository.findAllAvailable(any(), any()))
                .thenReturn(List.of(policy));

        when(userCouponRepository.findByUserId(1L))
                .thenReturn(List.of());

        BookCouponQuery query = BookCouponQuery.builder()
                .userId(1L)
                .bookId(100L)
                .primaryCategoryId(10L)
                .secondaryCategoryId(null)
                .build();

        List<CategoryCouponResponse> result =
                couponPolicyService.getAvailableCouponsForBook(query);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deleteCouponPolicy: 정책이 없으면 CouponPolicyNotFoundException")
    void deleteCouponPolicy_notFound_throws() {
        // given
        Long policyId = 999L;
        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponPolicyService.deleteCouponPolicy(policyId))
                .isInstanceOf(CouponPolicyNotFoundException.class);

        verify(couponPolicyRepository).findById(policyId);
        verifyNoMoreInteractions(userCouponRepository, couponCategoryRepository, couponBookRepository);
    }

    @Test
    @DisplayName("deleteCouponPolicy: 이미 발급된 쿠폰이 있으면 CouponPolicyDeleteNotAllowedException")
    void deleteCouponPolicy_issuedExists_throws() {
        // given
        Long policyId = 1L;
        CouponPolicy policy = mock(CouponPolicy.class);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.countByCouponPolicy_CouponPolicyId(policyId)).thenReturn(1L);

        // when & then
        assertThatThrownBy(() -> couponPolicyService.deleteCouponPolicy(policyId))
                .isInstanceOf(CouponPolicyDeleteNotAllowedException.class);

        // 삭제 관련 repo 호출이 일어나면 안됨
        verify(couponPolicyRepository).findById(policyId);
        verify(userCouponRepository).countByCouponPolicy_CouponPolicyId(policyId);
        verify(couponCategoryRepository, never()).deleteByCouponPolicy_CouponPolicyId(anyLong());
        verify(couponBookRepository, never()).deleteByCouponPolicy_CouponPolicyId(anyLong());
        verify(couponPolicyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteCouponPolicy: 발급된 쿠폰이 없으면 매핑 삭제 후 정책 삭제")
    void deleteCouponPolicy_notIssued_deletesMappingsAndPolicy() {
        // given
        Long policyId = 1L;
        CouponPolicy policy = mock(CouponPolicy.class);

        when(couponPolicyRepository.findById(policyId)).thenReturn(Optional.of(policy));
        when(userCouponRepository.countByCouponPolicy_CouponPolicyId(policyId)).thenReturn(0L);

        // when
        couponPolicyService.deleteCouponPolicy(policyId);

        // then (호출 순서까지 검증하면 더 깔끔)
        var inOrder = inOrder(couponPolicyRepository, userCouponRepository, couponCategoryRepository, couponBookRepository);
        inOrder.verify(couponPolicyRepository).findById(policyId);
        inOrder.verify(userCouponRepository).countByCouponPolicy_CouponPolicyId(policyId);
        inOrder.verify(couponCategoryRepository).deleteByCouponPolicy_CouponPolicyId(policyId);
        inOrder.verify(couponBookRepository).deleteByCouponPolicy_CouponPolicyId(policyId);
        inOrder.verify(couponPolicyRepository).delete(policy);
    }





}