package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CategoryCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryCouponRepository extends JpaRepository<CategoryCoupon, Long> {

    // 특정 카테고리에 사용 가능한 쿠폰 정책 조회
    List<CategoryCoupon> findByBookCategoryId(Long categoryId);

    // 특정 정책의 적용 카테고리 조회
    List<CategoryCoupon> findByCouponPolicy_CouponPolicyId(Long couponPolicyId);

    // 추가: 여러 카테고리 ID를 받아서 이름 매핑 조회
    @Query("SELECT DISTINCT cc.bookCategoryId, cc.couponCategoryName " +
            "FROM CategoryCoupon cc " +
            "WHERE cc.bookCategoryId IN :categoryIds")
    List<Object[]> findCategoryNamesByIds(@Param("categoryIds") List<Long> categoryIds);
}
