package com.nhnacademy.coupon.domain.coupon.service;

import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponJdbcRepository;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthdayCouponBulkService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponJdbcRepository userCouponJdbcRepository;

    @Transactional
    public long issueBirthdayCouponsBulk(List<Long> userIds) {
        // 1) 활성 BIRTHDAY 정책 1개 조회 (기존 로직 재사용 느낌)
        CouponPolicy policy = couponPolicyRepository.findByCouponType(CouponType.BIRTHDAY).stream()
                .filter(p -> p.getCouponPolicyStatus() == CouponPolicyStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("활성화된 생일 쿠폰 정책이 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = calculateExpiryDate(policy, now);

        // 2) JDBC batch insert
        int[] result = userCouponJdbcRepository.batchInsertBirthdayCoupons(
                userIds,
                policy.getCouponPolicyId(),
                now,
                expiryAt
        );

        // 성공 건수 집계 (드라이버에 따라 값이 -2(성공 but count unknown)일 수 있음)
        long success = Arrays.stream(result).filter(x -> x > 0 || x == -2).count();
        return success;
    }

    private LocalDateTime calculateExpiryDate(CouponPolicy policy, LocalDateTime issueTime) {
        if (policy.getValidDays() != null) return issueTime.plusDays(policy.getValidDays());
        if (policy.getValidEndDate() != null) return policy.getValidEndDate();
        return issueTime.plusYears(1);
    }
}

