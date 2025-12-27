package com.nhnacademy.coupon.domain.coupon.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserCouponJdbcRepository.class)
@ActiveProfiles("test")
class UserCouponJdbcRepositoryTest {

    @MockitoBean(name = "jpaAuditingHandler")
    Object jpaAuditingHandler;

    @MockitoBean(name = "jpaMappingContext")
    Object jpaMappingContext;

    @Autowired
    private UserCouponJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("batchInsertBirthdayCoupons - 정상 insert + IGNORE 동작")
    void batchInsert_success_and_ignoreDuplicate() {
        // given
        List<Long> userIds = List.of(1L, 2L, 3L);
        Long policyId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // when
        int[] result = repository.batchInsertBirthdayCoupons(
                userIds,
                policyId,
                now,
                now.plusDays(30)
        );

        // then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_coupons",
                Integer.class
        );

        assertThat(count).isEqualTo(3);

        // 중복 insert → IGNORE
        repository.batchInsertBirthdayCoupons(
                List.of(1L, 2L),
                policyId,
                now,
                now.plusDays(30)
        );

        Integer countAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_coupons",
                Integer.class
        );

        assertThat(countAfter).isEqualTo(3);
    }
}
