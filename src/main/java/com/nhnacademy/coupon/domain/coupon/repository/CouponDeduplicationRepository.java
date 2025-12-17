package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.saga.CouponDeduplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponDeduplicationRepository extends JpaRepository<CouponDeduplicationLog, Long> {
}
