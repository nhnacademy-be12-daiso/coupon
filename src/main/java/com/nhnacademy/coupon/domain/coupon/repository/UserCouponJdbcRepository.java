package com.nhnacademy.coupon.domain.coupon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCouponJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * userIds를 생일쿠폰으로 일괄 발급 (batch insert)
     */
    public int[] batchInsertBirthdayCoupons(
            List<Long> userIds, // 쿠폰을 발급할 사용자 ID 목록
            Long couponPolicyId, // 어떤 쿠폰 정책으로 발급할지
            LocalDateTime issuedAt, // 발급 시각
            LocalDateTime expiryAt){ // 만료 시각
        String sql = """
            INSERT INTO user_coupons
              (user_created_id, coupon_policy_id, status, issue_at, expiry_at)
            VALUES (?, ?, ?, ?, ?)
            
            """;

        int batchSize = 100;

        List<int []> results = new ArrayList<>();

        for (int i = 0; i < userIds.size(); i += batchSize) {
            List<Long> chunk = userIds.subList(i, Math.min(i + batchSize, userIds.size()));

            int[] r = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() { // 실제 DB에 save날리는부분
                @Override
                public void setValues(PreparedStatement ps, int idx) throws SQLException {
                    Long userId = chunk.get(idx);
                    ps.setLong(1, userId);
                    ps.setLong(2, couponPolicyId);
                    ps.setString(3, "ISSUED"); // CouponStatus.ISSUED 와 매핑
                    ps.setTimestamp(4, Timestamp.valueOf(issuedAt));
                    ps.setTimestamp(5, Timestamp.valueOf(expiryAt));
                }

                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });

            results.add(r);
        }

        // 합쳐서 반환(원하면 성공 건수만 합산해도 됨)
        return results.stream().flatMapToInt(arr -> Arrays.stream(arr)).toArray();

    }
}