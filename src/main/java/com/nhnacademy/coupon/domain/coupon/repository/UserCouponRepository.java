package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 특정 사용자의 모든 쿠폰 조회 (페이징)
    Page<UserCoupon> findByUserId(Long userId, Pageable pageable);

    // 특정 사용자의 특정 상태 쿠폰 조회
    List<UserCoupon> findByUserIdAndStatus(Long userId, CouponStatus status);

    // 특정 사용자의 사용 가능한 쿠폰 조회 (ISSUED 또는 CANCELED)
    List<UserCoupon> findByUserIdAndStatusIn(Long userId, List<CouponStatus> statuses);

    boolean existsByUserIdAndCouponPolicy_CouponPolicyId(Long userId, Long couponPolicyId);

    List<UserCoupon> findAllByStatusAndExpiryAtBefore(CouponStatus status, LocalDateTime expiryAtBefore);

    long countByCouponPolicyCouponPolicyId(Long couponPolicyId); // <-- 이 줄로 변경
}