package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponCategory;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CouponCategoryRepositoryTest {

    @Autowired
    CouponCategoryRepository couponCategoryRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("findByCouponPolicy_CouponPolicyIdIn: 여러 정책 ID에 매핑된 카테고리 조회")
    void findByCouponPolicyIdIn() {
        // given
        CouponPolicy p1 = savePolicy();
        CouponPolicy p2 = savePolicy();
        CouponPolicy p3 = savePolicy();

        saveCategoryMapping(p1, 10L);
        saveCategoryMapping(p1, 11L);
        saveCategoryMapping(p2, 20L);
        saveCategoryMapping(p3, 30L);

        em.flush();
        em.clear();

        // when
        List<CouponCategory> result =
                couponCategoryRepository.findByCouponPolicy_CouponPolicyIdIn(List.of(p1.getCouponPolicyId(), p2.getCouponPolicyId()));

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(CouponCategory::getCategoryId)
                .containsExactlyInAnyOrder(10L, 11L, 20L);


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

    private CouponCategory saveCategoryMapping(CouponPolicy policy, Long categoryId) {
        CouponCategory mapping = CouponCategory.builder()
                .couponPolicy(policy)
                .categoryId(categoryId)
                .build();

        em.persist(mapping);
        return mapping;
    }
}