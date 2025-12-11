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

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponPolicy WHERE uc.userId = :userId")
    List<UserCoupon> findByUserId(@Param("userId") Long userId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.couponPolicy " +
            "WHERE uc.userId = :userId AND uc.status = :status")
    List<UserCoupon> findByUserIdAndStatus(@Param("userId") Long userId,
                                           @Param("status") CouponStatus status);

    // 특정 사용자의 사용 가능한 쿠폰 조회 (ISSUED 또는 CANCELED)
    List<UserCoupon> findByUserIdAndStatusIn(Long userId, List<CouponStatus> statuses);

    // 특정 정책에 대한 쿠폰을 이 유저가 이미 가지고 있는지
    boolean existsByUserIdAndCouponPolicy_CouponPolicyId(Long userId, Long couponPolicyId);

    List<UserCoupon> findAllByStatusAndExpiryAtBefore(CouponStatus status,
                                                      LocalDateTime expiryAtBefore);

    // 발급된 쿠폰 개수
    long countByCouponPolicy_CouponPolicyId(Long couponPolicyId);

    @Modifying
    @Query("UPDATE UserCoupon uc " +
            "SET uc.status = 'EXPIRED' " +
            "WHERE uc.status = 'ISSUED' AND uc.expiryAt < :now")
    int bulkExpireCoupons(@Param("now") LocalDateTime now);

}
