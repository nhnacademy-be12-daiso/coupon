package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.BookCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookCouponRepository extends JpaRepository<BookCoupon, Long> {

    List<BookCoupon> findByBookId(Long bookId);

    List<BookCoupon> findByCouponPolicy_CouponPolicyId(Long couponPolicyId);
}