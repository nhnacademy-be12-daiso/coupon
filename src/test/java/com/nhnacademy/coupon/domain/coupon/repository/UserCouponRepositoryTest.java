package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
class UserCouponRepositoryTest {

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponPolicyRepository couponPolicyRepository;

    @Autowired
    private TestEntityManager em;

    private CouponPolicy couponPolicy;
    private Long testUserId = 1000L;

    @BeforeEach
    void setUp(){
        userCouponRepository.deleteAll();
        couponPolicyRepository.deleteAll();

        couponPolicy = CouponPolicy.builder()
                .couponPolicyName("테스트 쿠폰")
                .couponType(CouponType.WELCOME)
                .discountWay(DiscountWay.FIXED)
                .discountAmount(BigDecimal.valueOf(10000))
                .minOrderAmount(50000L)
                .validDays(30)
                .quantity(null)
                .couponPolicyStatus(CouponPolicyStatus.ACTIVE)
                .build();
        couponPolicyRepository.save(couponPolicy);
    }


    private UserCoupon issueCoupon(Long userId, CouponStatus status, LocalDateTime issuedAt, LocalDateTime expiryAt) {
        UserCoupon uc = UserCoupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .status(status)
                .issuedAt(issuedAt)
                .expiryAt(expiryAt)
                .usedAt(null)
                .usedOrderId(null)
                .build();
        return userCouponRepository.save(uc);
    }

    @Test
    @DisplayName("사용자 쿠폰 발급 테스트")
    void testIssueCoupon(){
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(30);

        // when
        UserCoupon saved = issueCoupon(testUserId, CouponStatus.ISSUED, now, expiry);

        // then
        assertThat(saved.getUserCouponId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(testUserId);
        assertThat(saved.getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(saved.getIssuedAt()).isEqualTo(now);
        assertThat(saved.getExpiryAt()).isEqualTo(expiry);
        assertThat(saved.getCouponPolicy()).isNotNull();
        assertThat(saved.getCouponPolicy().getCouponPolicyId()).isEqualTo(couponPolicy.getCouponPolicyId());

    }
    @Test
    @DisplayName("findByUserId: userId로 쿠폰 조회 + couponPolicy fetch join")
    void testFindByUserId() {
        // given
        LocalDateTime now = LocalDateTime.now();
        issueCoupon(testUserId, CouponStatus.ISSUED, now, now.plusDays(10));
        issueCoupon(testUserId, CouponStatus.USED, now.minusDays(1), now.plusDays(10));
        issueCoupon(2000L, CouponStatus.ISSUED, now, now.plusDays(10)); // 다른 유저
        // issueCoupon()을 하면 persist() 상태이기 떄문에 영속성 컨텍스트에만 저장됨
        // flush가 없으면 DB 기준으로는 데이터가 없을 수도 있다. 그래서 flush로 db에 박아놓고 em.clear로 영속성 컨텍스트를 비운다.
        em.flush();
        em.clear();

        // when
        List<UserCoupon> result = userCouponRepository.findByUserId(testUserId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(uc -> {
            assertThat(uc.getUserId()).isEqualTo(testUserId);
            // fetch join 검증 포인트: couponPolicy가 null 아니고, id 접근 가능
            assertThat(uc.getCouponPolicy()).isNotNull();
            assertThat(uc.getCouponPolicy().getCouponPolicyId()).isNotNull();
        });
    }

    @Test
    @DisplayName("findByUserIdAndStatus: userId + status로 조회")
    void testFindByUserIdAndStatus() {
        // given
        LocalDateTime now = LocalDateTime.now();
        issueCoupon(testUserId, CouponStatus.ISSUED, now, now.plusDays(10));
        issueCoupon(testUserId, CouponStatus.USED, now.minusDays(1), now.plusDays(10));

        em.flush();
        em.clear();

        // when
        List<UserCoupon> issued = userCouponRepository.findByUserIdAndStatus(testUserId, CouponStatus.ISSUED);

        // then
        assertThat(issued).hasSize(1);
        assertThat(issued.get(0).getStatus()).isEqualTo(CouponStatus.ISSUED);
        assertThat(issued.get(0).getCouponPolicy().getCouponPolicyId()).isEqualTo(couponPolicy.getCouponPolicyId());
    }

    @Test
    @DisplayName("findByUserIdAndStatusIn: 여러 status로 조회")
    void testFindByUserIdAndStatusIn() {
        // given
        LocalDateTime now = LocalDateTime.now();
        issueCoupon(testUserId, CouponStatus.ISSUED, now, now.plusDays(10));
        issueCoupon(testUserId, CouponStatus.USED, now.minusDays(1), now.plusDays(10));
        issueCoupon(testUserId, CouponStatus.EXPIRED, now.minusDays(40), now.minusDays(1));

        em.flush();
        em.clear();

        // when
        List<UserCoupon> result = userCouponRepository.findByUserIdAndStatusIn(
                testUserId,
                List.of(CouponStatus.ISSUED, CouponStatus.USED)
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserCoupon::getStatus)
                .containsExactlyInAnyOrder(CouponStatus.ISSUED, CouponStatus.USED);
    }

    @Test
    @DisplayName("existsByUserIdAndCouponPolicy_CouponPolicyId: 특정 정책 쿠폰 보유 여부")
    void testExistsByUserIdAndCouponPolicyId() {
        // given
        LocalDateTime now = LocalDateTime.now();
        issueCoupon(testUserId, CouponStatus.ISSUED, now, now.plusDays(10));

        em.flush();
        em.clear();

        // when
        boolean exists = userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(
                testUserId,
                couponPolicy.getCouponPolicyId()
        );
        boolean notExists = userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(
                9999L,
                couponPolicy.getCouponPolicyId()
        );

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("countByCouponPolicy_CouponPolicyId: 정책별 발급된 쿠폰 개수")
    void testCountByCouponPolicyId() {
        // given
        LocalDateTime now = LocalDateTime.now();
        issueCoupon(1000L, CouponStatus.ISSUED, now, now.plusDays(10));
        issueCoupon(2000L, CouponStatus.ISSUED, now, now.plusDays(10));
        issueCoupon(3000L, CouponStatus.USED, now.minusDays(1), now.plusDays(10));

        em.flush();
        em.clear();

        // when
        long count = userCouponRepository.countByCouponPolicy_CouponPolicyId(couponPolicy.getCouponPolicyId());

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("bulkExpireCoupons: 만료된 ISSUED 쿠폰을 EXPIRED로 벌크 업데이트")
    void testBulkExpireCoupons() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 만료 대상 (ISSUED && expiryAt < now)
        UserCoupon willExpire = issueCoupon(testUserId, CouponStatus.ISSUED, now.minusDays(10), now.minusDays(1));

        // 만료 대상 아님 (ISSUED지만 expiryAt >= now)
        issueCoupon(testUserId, CouponStatus.ISSUED, now.minusDays(1), now.plusDays(1));

        // 만료 대상 아님 (USED)
        issueCoupon(testUserId, CouponStatus.USED, now.minusDays(10), now.minusDays(1));

        em.flush();
        em.clear();

        // when
        int updated = userCouponRepository.bulkExpireCoupons(now);

        // then
        assertThat(updated).isEqualTo(1);

        em.flush();
        em.clear();

        UserCoupon reloaded = userCouponRepository.findById(willExpire.getUserCouponId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }
}