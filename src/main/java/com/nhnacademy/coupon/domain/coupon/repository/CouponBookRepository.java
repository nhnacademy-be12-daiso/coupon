package com.nhnacademy.coupon.domain.coupon.repository;

import com.nhnacademy.coupon.domain.coupon.entity.CouponBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponBookRepository extends JpaRepository<CouponBook, Long> {

    // 도서 상세에서 사용할 용도
    List<CouponBook> findByBookId(Long bookId);

    // 필요하면 여러 도서 한 번에 조회용
    List<CouponBook> findByBookIdIn(List<Long> bookIds);

}