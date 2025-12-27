package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.dto.message.CouponIssueMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageRecordTest {

    @Test
    void couponIssueMessage_record() {
        CouponIssueMessage m = new CouponIssueMessage(10L);
        assertThat(m.userCreatedId()).isEqualTo(10L);
    }

    @Test
    void birthdayCouponBulkEvent_record() {
        BirthdayCouponBulkEvent e = new BirthdayCouponBulkEvent(List.of(1L, 2L), "batch-1");
        assertThat(e.userIds()).containsExactly(1L, 2L);
        assertThat(e.batchId()).isEqualTo("batch-1");
    }
}
