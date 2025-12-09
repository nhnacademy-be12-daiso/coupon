package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

//    // 특정 사용자의 모든 쿠폰 조회
//    List<UserCoupon> findByUserId(Long userId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponPolicy WHERE uc.userId = :userId")
    List<UserCoupon> findByUserId(@Param("userId") Long userId);

    // 특정 사용자의 특정 상태 쿠폰 조회
    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponPolicy WHERE uc.userId = :userId AND uc.status = :status")
    List<UserCoupon> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CouponStatus status);

    // 특정 사용자의 사용 가능한 쿠폰 조회 (ISSUED 또는 CANCELED)
    List<UserCoupon> findByUserIdAndStatusIn(Long userId, List<CouponStatus> statuses);

    boolean existsByUserIdAndCouponPolicy_CouponPolicyId(Long userId, Long couponPolicyId);

    List<UserCoupon> findAllByStatusAndExpiryAtBefore(CouponStatus status, LocalDateTime expiryAtBefore);

    long countByCouponPolicyCouponPolicyId(Long couponPolicyId); // <-- 이 줄로 변경

    // UserCouponRepository.java에 추가
    @Modifying
    @Query("UPDATE UserCoupon uc SET uc.status = 'EXPIRED' WHERE uc.status = 'ISSUED' AND uc.expiryAt < :now")
    int bulkExpireCoupons(@Param("now") LocalDateTime now);

    // "정책 ID + 타겟 ID" 조합으로 이미 발급된 쿠폰이 있는지 확인
    boolean existsByUserIdAndCouponPolicy_CouponPolicyIdAndTargetId(
            Long userId,
            Long couponPolicyId,
            Long targetId
    );
}