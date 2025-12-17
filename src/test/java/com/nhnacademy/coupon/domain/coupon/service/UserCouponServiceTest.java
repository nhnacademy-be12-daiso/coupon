package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponCategory;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.repository.CouponBookRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponCategoryRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.impl.UserCouponServiceImpl;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCouponServiceTest {

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponPolicyRepository couponPolicyRepository;

    @Mock
    private CouponBookRepository couponBookRepository;

    @Mock
    private BookServiceClient bookServiceClient;

    @Mock
    private CouponCategoryRepository couponCategoryRepository;

    @InjectMocks UserCouponServiceImpl userCouponService;




    @Test
    @DisplayName("calculateDiscount: 최소 주문 금액 미달이면 할인 0")
    void calculateDiscount_belowMinOrderAmount_zero() {
        CouponPolicy policy = CouponPolicy.builder()
                .discountWay(DiscountWay.FIXED)
                .discountAmount(new BigDecimal("2000"))
                .minOrderAmount(20000L)
                .build();

        BigDecimal discount = userCouponService.calculateDiscount(policy, new BigDecimal("15000"));
        assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("calculateDiscount: FIXED 할인 정상 계산")
    void calculateDiscount_fixed_ok() {
        CouponPolicy policy = CouponPolicy.builder()
                .discountWay(DiscountWay.FIXED)
                .discountAmount(new BigDecimal("2000"))
                .minOrderAmount(10000L)
                .build();

        BigDecimal discount = userCouponService.calculateDiscount(policy, new BigDecimal("15000"));
        assertThat(discount).isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("calculateDiscount: RATE 할인 정상 계산")
    void calculateDiscount_rate_ok() {
        CouponPolicy policy = CouponPolicy.builder()
                .discountWay(DiscountWay.PERCENT)
                .discountAmount(new BigDecimal("10")) // 10%
                .minOrderAmount(10000L)
                .build();

        BigDecimal discount = userCouponService.calculateDiscount(policy, new BigDecimal("20000"));

        assertThat(discount).isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("calculateDiscount: 최대 할인 금액(maxDiscountAmount) 캡 적용")
    void calculateDiscount_maxCap_applied() {
        CouponPolicy policy = CouponPolicy.builder()
                .discountWay(DiscountWay.PERCENT)
                .discountAmount(new BigDecimal("50")) // 50%
                .minOrderAmount(0L)
                .maxDiscountAmount(3000L) // 캡 3000
                .build();

        BigDecimal discount = userCouponService.calculateDiscount(policy, new BigDecimal("10000")); // 50% => 5000

        assertThat(discount).isEqualByComparingTo("3000");
    }

    @Test
    @DisplayName("getAvailableCoupons: bookId 기준으로 적용 가능한 쿠폰만 반환 (BOOKS/CATEGORY/WELCOME)")
    void getAvailableCoupons_filtersByBook() {
        // given
        Long userId = 100L;
        Long bookId = 200L;

        // (1) book 카테고리: [10, 20]
        BookCategoryResponse bookInfo = new BookCategoryResponse();
        bookInfo.setPrimaryCategoryId(10L);
        bookInfo.setSecondaryCategoryId(20L);
        when(bookServiceClient.getBookCategory(bookId)).thenReturn(bookInfo);

        // (2) 쿠폰 정책 3개 (BOOKS / CATEGORY / WELCOME)
        CouponPolicy booksPolicy = buildPolicy(1L, CouponType.BOOKS);
        CouponPolicy categoryPolicy = buildPolicy(2L, CouponType.CATEGORY);
        CouponPolicy welcomePolicy = buildPolicy(3L, CouponType.WELCOME);

        // (3) 유저가 가진 ISSUED 쿠폰 3개 (만료 안 됨)
        UserCoupon booksCoupon = buildIssuedUserCoupon(userId, booksPolicy, LocalDateTime.now().plusDays(3));
        UserCoupon categoryCoupon = buildIssuedUserCoupon(userId, categoryPolicy, LocalDateTime.now().plusDays(3));
        UserCoupon welcomeCoupon = buildIssuedUserCoupon(userId, welcomePolicy, LocalDateTime.now().plusDays(3));

        // 서비스가 실제로 부르는 repo 메서드로 stub!
        when(userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED))
                .thenReturn(List.of(booksCoupon, categoryCoupon, welcomeCoupon));

        // (4) BOOKS 적용: policyId=1은 bookId=200에 매핑됨
        when(couponBookRepository.existsByCouponPolicy_CouponPolicyIdAndBookId(1L, bookId))
                .thenReturn(true);

        // (5) CATEGORY 적용: policyId=2가 categoryId=20에 매핑됨
        // 여기서 엔티티 mock 쓰지 말고 "진짜 객체"로 만들어서 반환
        CouponCategory mapping = new CouponCategory(null, categoryPolicy, 20L);
        when(couponCategoryRepository.findByCouponPolicy_CouponPolicyId(2L))
                .thenReturn(List.of(mapping));

        // when
        List<UserCouponResponse> res = userCouponService.getAvailableCoupons(userId, bookId);

        // then
        assertThat(res).hasSize(3);
        assertThat(res).allSatisfy(r -> assertThat(r.getStatus()).isEqualTo(CouponStatus.ISSUED));
    }

    @Test
    @DisplayName("getAvailableCoupons: bookId가 null이면(마이페이지) ISSUED 쿠폰 전체 반환 + bookServiceClient 호출 안 함")
    void getAvailableCoupons_whenBookIdNull_returnsAllIssuedNotExpired() {
        Long userId = 100L;

        CouponPolicy welcome = buildPolicy(1L, CouponType.WELCOME);
        CouponPolicy books = buildPolicy(2L, CouponType.BOOKS);

        UserCoupon notExpired1 = buildIssuedUserCoupon(userId, welcome, LocalDateTime.now().plusDays(1));
        UserCoupon notExpired2 = buildIssuedUserCoupon(userId, books, LocalDateTime.now().plusDays(1));
        UserCoupon expired = buildIssuedUserCoupon(userId, welcome, LocalDateTime.now().minusDays(1)); // 만료

        when(userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED))
                .thenReturn(List.of(notExpired1,notExpired2,expired));

        List<UserCouponResponse> res = userCouponService.getAvailableCoupons(userId, null);

        assertThat(res).hasSize(2);

        Mockito.verifyNoInteractions(bookServiceClient);
        Mockito.verifyNoInteractions(couponBookRepository);
        Mockito.verifyNoInteractions(couponCategoryRepository);
    }


        private CouponPolicy buildPolicy(Long policyId, CouponType type) {
        return CouponPolicy.builder()
                .couponPolicyId(policyId)
                .couponType(type)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("getAvailableCoupons: BOOKS 쿠폰이 bookId와 매핑 안 되면 제외된다")
    void getAvailableCoupons_excludesBooksWehnNoMapped(){
        Long userId = 100L;
        Long bookId = 200L;

        BookCategoryResponse bookInfo = new BookCategoryResponse();
        bookInfo.setPrimaryCategoryId(10L);
        when(bookServiceClient.getBookCategory(bookId)).thenReturn(bookInfo);

        CouponPolicy booksPolicy = buildPolicy(1L, CouponType.BOOKS);
        UserCoupon booksCoupons = buildIssuedUserCoupon(userId, booksPolicy, LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED))
                .thenReturn(List.of(booksCoupons));

        when(couponBookRepository.existsByCouponPolicy_CouponPolicyIdAndBookId(1L, bookId))
                .thenReturn(false);

        List<UserCouponResponse> res = userCouponService.getAvailableCoupons(userId, bookId);

        assertThat(res).isEmpty();

    }

    @Test
    @DisplayName("getAvailableCoupons: CATEGORY 쿠폰이 책 카테고리와 매핑 안 되면 제외된다")
    void getAvailableCoupons_excludesCategoryWhenNotMatched() {
        Long userId = 100L;
        Long bookId = 200L;

        BookCategoryResponse bookInfo = new BookCategoryResponse();
        bookInfo.setPrimaryCategoryId(10L);
        bookInfo.setSecondaryCategoryId(20L);
        when(bookServiceClient.getBookCategory(bookId)).thenReturn(bookInfo);

        CouponPolicy categoryPolicy = buildPolicy(2L, CouponType.CATEGORY);
        UserCoupon categoryCoupon = buildIssuedUserCoupon(userId, categoryPolicy, LocalDateTime.now().plusDays(1));

        when(userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED))
                .thenReturn(List.of(categoryCoupon));

        // policy 2는 category 999에만 매핑 -> 책(10/20)과 불일치 -> 제외
        CouponCategory mapping = new CouponCategory(null, categoryPolicy, 999L);
        when(couponCategoryRepository.findByCouponPolicy_CouponPolicyId(2L))
                .thenReturn(List.of(mapping));

        List<UserCouponResponse> res = userCouponService.getAvailableCoupons(userId, bookId);

        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("getAvailableCoupons: 책 카테고리 정보 없으면 CATEGORY/BOOKS 제외, 범용 쿠폰은 포함")
    void getAvailableCoupons_whenBookHasNoCategories_filtersCorrectly() {
        Long userId = 100L;
        Long bookId = 200L;

        BookCategoryResponse bookInfo = new BookCategoryResponse(); // primary/secondary null
        when(bookServiceClient.getBookCategory(bookId)).thenReturn(bookInfo);

        CouponPolicy welcome = buildPolicy(1L, CouponType.WELCOME);
        CouponPolicy category = buildPolicy(2L, CouponType.CATEGORY);
        CouponPolicy books = buildPolicy(3L, CouponType.BOOKS);

        when(userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED))
                .thenReturn(List.of(
                        buildIssuedUserCoupon(userId, welcome,  LocalDateTime.now().plusDays(1)),
                        buildIssuedUserCoupon(userId, category, LocalDateTime.now().plusDays(1)),
                        buildIssuedUserCoupon(userId, books,    LocalDateTime.now().plusDays(1))
                ));

        List<UserCouponResponse> res = userCouponService.getAvailableCoupons(userId, bookId);

        assertThat(res).hasSize(1); // welcome만 남아야 함
        assertThat(res.get(0).getCouponPolicy().couponType()).isEqualTo(CouponType.WELCOME);
    }


    private UserCoupon buildIssuedUserCoupon(Long userId, CouponPolicy policy, LocalDateTime expiryAt) {
        return UserCoupon.builder()
                .userId(userId)
                .couponPolicy(policy)
                .status(CouponStatus.ISSUED)
                .issuedAt(LocalDateTime.now().minusDays(1))
                .expiryAt(expiryAt)
                .build();
    }


}