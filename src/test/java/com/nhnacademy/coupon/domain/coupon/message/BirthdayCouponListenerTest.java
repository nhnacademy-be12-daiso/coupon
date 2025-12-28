package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

class BirthdayCouponListenerTest {

    @Mock CouponPolicyService couponPolicyService;
    @Mock RabbitTemplate rabbitTemplate;
    @Mock Channel channel;

    BirthdayCouponListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new BirthdayCouponListener(couponPolicyService, rabbitTemplate);

        // @Value 필드 주입
        ReflectionTestUtils.setField(listener, "retryExchange", "birthday.retry.ex");
        ReflectionTestUtils.setField(listener, "retryRoutingKey", "birthday.retry.key");
        ReflectionTestUtils.setField(listener, "dlxExchange", "birthday.dlx.ex");
        ReflectionTestUtils.setField(listener, "dlqRoutingKey", "birthday.dlq.key");
    }

    private Message messageWith(long tag, int retryCount) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(tag);
        props.getHeaders().put("x-retry-count", retryCount);
        return new Message("{}".getBytes(), props);
    }

    @Test
    @DisplayName("성공: issueBirthdayCouponsBulk 호출 + ACK")
    void success_ack() throws Exception {
        BirthdayCouponBulkEvent event = new BirthdayCouponBulkEvent(List.of(1L, 2L), "batch-1");
        Message msg = messageWith(10L, 0);

        listener.handleBirthdayCouponEvent(event, msg, channel);

        verify(couponPolicyService).issueBirthdayCouponsBulk(List.of(1L, 2L));
        verify(channel).basicAck(10L, false);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("실패 + 재발행 성공(재시도<=3): retry로 send + 원본 ACK")
    void fail_republishToRetry_and_ack() throws Exception {
        BirthdayCouponBulkEvent event = new BirthdayCouponBulkEvent(List.of(1L), "batch-2");
        Message msg = messageWith(11L, 0);

        doThrow(new RuntimeException("boom"))
                .when(couponPolicyService).issueBirthdayCouponsBulk(anyList());

        listener.handleBirthdayCouponEvent(event, msg, channel);

        // retryCount 0 -> nextRetry 1
        verify(rabbitTemplate).send(eq("birthday.retry.ex"), eq("birthday.retry.key"), any(Message.class));
        verify(channel).basicAck(11L, false);
    }

    @Test
    @DisplayName("실패 + 재발행도 실패: basicNack(requeue=true)")
    void fail_republishFail_nackRequeue() throws Exception {
        BirthdayCouponBulkEvent event = new BirthdayCouponBulkEvent(List.of(1L), "batch-3");
        Message msg = messageWith(12L, 1);

        doThrow(new RuntimeException("boom"))
                .when(couponPolicyService).issueBirthdayCouponsBulk(anyList());

        doThrow(new RuntimeException("publish fail"))
                .when(rabbitTemplate).send(anyString(), anyString(), any(Message.class));

        listener.handleBirthdayCouponEvent(event, msg, channel);

        verify(channel).basicNack(12L, false, true);
    }

    @Test
    @DisplayName("실패 + 재시도 초과: DLQ로 send + 원본 ACK")
    void fail_overMax_toDlq_and_ack() throws Exception {
        BirthdayCouponBulkEvent event = new BirthdayCouponBulkEvent(List.of(1L), "batch-4");
        // retryCount 3 -> nextRetry 4 (MAX_RETRY=3 초과) => DLQ
        Message msg = messageWith(13L, 3);

        doThrow(new RuntimeException("boom"))
                .when(couponPolicyService).issueBirthdayCouponsBulk(anyList());

        listener.handleBirthdayCouponEvent(event, msg, channel);

        verify(rabbitTemplate).send(eq("birthday.dlx.ex"), eq("birthday.dlq.key"), any(Message.class));
        verify(channel).basicAck(13L, false);
    }
}
