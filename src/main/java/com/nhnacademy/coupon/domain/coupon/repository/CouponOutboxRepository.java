package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.saga.CouponOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponOutboxRepository extends JpaRepository<CouponOutbox, Long> {
}
