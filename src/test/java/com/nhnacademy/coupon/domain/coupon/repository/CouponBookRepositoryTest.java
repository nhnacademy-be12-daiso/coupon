package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponBook;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.persistence.EntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CouponBookRepositoryTest {

    @Autowired
    CouponBookRepository couponBookRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("findByBookId: 특정 도서 ID에 매핑된 쿠폰 정책 매핑 row들 조회")
    void findByBookId() {
        // given
        CouponPolicy p1 = savePolicy();
        CouponPolicy p2 = savePolicy();

        saveBookMapping(p1, 100L);
        saveBookMapping(p2, 100L);
        saveBookMapping(p2, 200L);

        em.flush();
        em.clear();

        // when
        List<CouponBook> result = couponBookRepository.findByBookId(100L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CouponBook::getBookId)
                .containsOnly(100L);
    }

    @Test
    @DisplayName("findByBookIdIn: 여러 도서 ID로 한 번에 매핑 row들 조회")
    void findByBookIdIn() {
        // given
        CouponPolicy p1 = savePolicy();
        CouponPolicy p2 = savePolicy();
        CouponPolicy p3 = savePolicy();

        saveBookMapping(p1, 10L);
        saveBookMapping(p2, 20L);
        saveBookMapping(p3, 30L);

        em.flush();
        em.clear();

        // when
        List<CouponBook> result = couponBookRepository.findByBookIdIn(List.of(10L, 30L, 999L));

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CouponBook::getBookId)
                .containsExactlyInAnyOrder(10L, 30L);
    }

    @Test
    @DisplayName("existsByCouponPolicy_CouponPolicyIdAndBookId: 해당 정책-도서 매핑 존재 여부")
    void existsByPolicyIdAndBookId() {
        // given
        CouponPolicy p1 = savePolicy();
        CouponPolicy p2 = savePolicy();

        saveBookMapping(p1, 777L);

        em.flush();
        em.clear();

        // when & then
        assertThat(couponBookRepository.existsByCouponPolicy_CouponPolicyIdAndBookId(p1.getCouponPolicyId(), 777L))
                .isTrue();

        assertThat(couponBookRepository.existsByCouponPolicy_CouponPolicyIdAndBookId(p1.getCouponPolicyId(), 888L))
                .isFalse();

        assertThat(couponBookRepository.existsByCouponPolicy_CouponPolicyIdAndBookId(p2.getCouponPolicyId(), 777L))
                .isFalse();
    }

    // ====== helper ======

    private CouponPolicy savePolicy() {
        CouponPolicy policy = CouponPolicy.builder()
                .couponPolicyName("테스트 정책")
                .couponType(CouponType.CATEGORY)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(new BigDecimal("1000"))
                .minOrderAmount(0L)
                .maxDiscountAmount(0L)
                .validDays(10) // 상대 유효기간 쓰면 이것만
                .quantity(null) // null이면 무제한 컨셉
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE) // 너 enum에 맞게
                .build();

        em.persist(policy);
        return policy;
    }

    private CouponBook saveBookMapping(CouponPolicy policy, Long bookId) {
        CouponBook mapping = CouponBook.builder()
                .couponPolicy(policy)
                .bookId(bookId)
                .build();

        em.persist(mapping);
        return mapping;
    }
}
