package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CouponCategoryRepository extends JpaRepository<CouponCategory, Long> {

    // 특정 정책들에 매핑된 카테고리들 (마이페이지 등)
    List<CouponCategory> findByCouponPolicy_CouponPolicyIdIn(Collection<Long> couponPolicyIds);

    // 특정 정책에 매핑된 카테고리들
    List<CouponCategory> findByCouponPolicy_CouponPolicyId(Long couponPolicyId);

    // 특정 카테고리(여러 개)에 매핑된 정책들
    List<CouponCategory> findByCategoryIdIn(Collection<Long> categoryIds);
}
