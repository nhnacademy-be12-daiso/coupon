package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest  // JPA 테스트용 어노테이션
@ActiveProfiles("test")
class CouponPolicyRepositoryTest {

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    private CouponPolicy welcomePolicy;
    private CouponPolicy birthdayPolicy;
    private CouponPolicy bookPolicy;

    @BeforeEach
    void setUp(){
        couponPolicyRepository.deleteAll();

        welcomePolicy = CouponPolicy.builder()
                .couponPolicyName("신규 가입 축하 쿠폰")
                .couponType(CouponType.WELCOME)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(10000))
                .minOrderAmount(50000L)
                .validDays(30)
                .quantity(null)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();

        birthdayPolicy = CouponPolicy.builder()
                .couponPolicyName("생일 축하 쿠폰")
                .couponType(CouponType.BIRTHDAY)
                .discountWay(DiscountWay.PERCENT)
                .discountAmount(BigDecimal.valueOf(20))
                .minOrderAmount(30000L)
                .validDays(7)
                .quantity(null)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();

        bookPolicy = CouponPolicy.builder()
                .couponPolicyName("IT 도서 할인 쿠폰")
                .couponType(CouponType.BOOKS)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(5000))
                .minOrderAmount(20000L)
                .validDays(90)
                .quantity(1000)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();

        couponPolicyRepository.saveAll(List.of(welcomePolicy, birthdayPolicy, bookPolicy));

    }

    @Test
    @DisplayName("쿠폰 정책 저장 테스트")
    void testSaveCouponPolicy() {
        // When
        CouponPolicy saved = couponPolicyRepository.save(
                CouponPolicy.builder()
                        .couponPolicyName("특별 할인 쿠폰")
                        .couponType(CouponType.GENERAL)
                        .discountWay(DiscountWay.FIXED)
                        .discountAmount(BigDecimal.valueOf(3000))
                        .minOrderAmount(15000L)
                        .validDays(14)
                        .quantity(500)
                        .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                        .build()
        );

        // Then
        assertThat(saved.getCouponPolicyId()).isNotNull();
        assertThat(saved.getCouponPolicyName()).isEqualTo("특별 할인 쿠폰");
    }

    @Test
    @DisplayName("쿠폰 타입으로 정책 조회 테스트")
    void testFindByCouponType(){
        // when
        List<CouponPolicy> welcomePolicies = couponPolicyRepository
                .findByCouponType(CouponType.WELCOME);

        // Then
        assertThat(welcomePolicies).hasSize(1);
        assertThat(welcomePolicies.get(0).getCouponPolicyName())
                .isEqualTo("신규 가입 축하 쿠폰");
    }

    @Test
    @DisplayName("활성 상태 정책만 조회 테스트")
    void testFindActivePolices(){
        // given
        CouponPolicy inactivePolicy = CouponPolicy.builder()
                .couponPolicyName("만료된 쿠폰")
                .couponType(CouponType.GENERAL)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(1000))
                .minOrderAmount(10000L)
                .validDays(1)
                .quantity(0)
                .couponPolicyStatus(CouponPolicyStatus.DELETED)
                .build();
        couponPolicyRepository.save(inactivePolicy);

        //when
        List<CouponPolicy> activePolicies = couponPolicyRepository.findByCouponPolicyStatus(CouponPolicyStatus.ACTIVE);

        assertThat(activePolicies).hasSize(3);
        assertThat(activePolicies).allMatch(p -> p.getCouponPolicyStatus() == CouponPolicyStatus.ACTIVE);

    }

    @Test
    @DisplayName("수량 제한이 있는 정책 조회 테스트")
    void testFindPoliciesWithQuantity(){
        // when
        List<CouponPolicy> policiesWithQuantity = couponPolicyRepository.findAll()
                .stream()
                .filter(p -> p.getQuantity() != null)
                .toList();

        // then
        assertThat(policiesWithQuantity).hasSize(1);
        assertThat(policiesWithQuantity.get(0).getCouponPolicyName())
                .isEqualTo("IT 도서 할인 쿠폰");

    }

    @Test
    @DisplayName("정책 업데이트 테스트")
    void testUpdateCouponPolicy(){
        // when
        welcomePolicy.updateStatus(CouponPolicyStatus.DELETED);
//        CouponPolicy updated = couponPolicyRepository.save(welcomePolicy);
        assertThat(welcomePolicy.getCouponPolicyStatus()).isEqualTo(CouponPolicyStatus.DELETED);

    }

    @Test
    @DisplayName("정책 삭제 테스트")
    void testDeleteCouponPolicy(){
        // when
        Long policyId = welcomePolicy.getCouponPolicyId();
        couponPolicyRepository.delete(welcomePolicy);
        Optional<CouponPolicy> deleted = couponPolicyRepository.findById(policyId);

        assertThat(deleted).isEmpty();

    }

    @Test
    @DisplayName("할인 방식별 정책 조회 테스트")
    void testFindByDiscountWay() {
        // When
        List<CouponPolicy> fixedPolicies = couponPolicyRepository.findAll()
                .stream()
                .filter(p -> p.getDiscountWay() == DiscountWay.FIXED)
                .toList();

        List<CouponPolicy> percentagePolicies = couponPolicyRepository.findAll()
                .stream()
                .filter(p -> p.getDiscountWay() == DiscountWay.PERCENT)
                .toList();

        // Then
        assertThat(fixedPolicies).hasSize(2);
        assertThat(percentagePolicies).hasSize(1);
    }
}