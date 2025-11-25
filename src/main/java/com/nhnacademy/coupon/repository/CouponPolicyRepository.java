package com.nhnacademy.coupon.repository;

import com.nhnacademy.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponPolicyRepository extends JpaRepository<CouponPolicy, Long> {
    // CouponType으로 조회 (WELCOME, BIRTHDAY 등)
    List<CouponPolicy> findByCouponType(CouponType couponType);
}