package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {
    // CouponType으로 조회 (WELCOME, BIRTHDAY 등)
    List<CouponPolicy> findByCouponType(CouponType couponType);

    // ACTIVE 상태만 조회
    List<CouponPolicy> findByCouponPolicyStatus(CouponPolicyStatus status);


    @Query("""
        SELECT p
        FROM CouponPolicy p
        WHERE p.couponPolicyStatus = :status
          AND (p.validStartDate IS NULL OR p.validStartDate <= :now)
          AND (p.validEndDate   IS NULL OR p.validEndDate   >= :now)
    """)
    List<CouponPolicy> findAllAvailable(@Param("status") CouponPolicyStatus status,
                                        @Param("now") LocalDateTime now);
}