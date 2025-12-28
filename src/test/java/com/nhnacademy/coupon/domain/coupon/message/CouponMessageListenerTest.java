package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.dto.message.CouponIssueMessage;
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

import static org.mockito.Mockito.*;

class CouponMessageListenerTest {

    @Mock CouponPolicyService couponPolicyService;
    @Mock RabbitTemplate rabbitTemplate;
    @Mock Channel channel;

    CouponMessageListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new CouponMessageListener(couponPolicyService, rabbitTemplate);

        ReflectionTestUtils.setField(listener, "retryExchangeName", "welcome.retry.ex");
        ReflectionTestUtils.setField(listener, "retryRoutingKey", "welcome.retry.key");
        ReflectionTestUtils.setField(listener, "maxAttempts", 3);
        ReflectionTestUtils.setField(listener, "dlxName", "welcome.dlx.ex");
        ReflectionTestUtils.setField(listener, "dlqRoutingKey", "welcome.dlq.key");
    }

    private Message msg(long tag, int retryCount) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(tag);
        props.getHeaders().put("x-retry-count", retryCount);
        return new Message("{}".getBytes(), props);
    }

    @Test
    @DisplayName("성공: issueWelcomeCoupon 호출 + ACK")
    void success_ack() throws Exception {
        CouponIssueMessage payload = new CouponIssueMessage(100L);
        Message message = msg(20L, 0);

        listener.handleWelcomeCouponIssue(payload, message, channel);

        verify(couponPolicyService).issueWelcomeCoupon(100L);
        verify(channel).basicAck(20L, false);
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("실패 + 재시도 가능: retry로 send + ACK")
    void fail_retry_send_and_ack() throws Exception {
        CouponIssueMessage payload = new CouponIssueMessage(101L);
        Message message = msg(21L, 0);

        doThrow(new RuntimeException("boom"))
                .when(couponPolicyService).issueWelcomeCoupon(101L);

        listener.handleWelcomeCouponIssue(payload, message, channel);

        verify(rabbitTemplate).send(eq("welcome.retry.ex"), eq("welcome.retry.key"), same(message));
        verify(channel).basicAck(21L, false);
    }

    @Test
    @DisplayName("실패 + 재시도 초과: DLQ로 send + ACK")
    void fail_dlq_send_and_ack() throws Exception {
        CouponIssueMessage payload = new CouponIssueMessage(102L);
        // retryCount 3 -> next 4 > maxAttempts(3) => DLQ
        Message message = msg(22L, 3);

        doThrow(new RuntimeException("boom"))
                .when(couponPolicyService).issueWelcomeCoupon(102L);

        listener.handleWelcomeCouponIssue(payload, message, channel);

        verify(rabbitTemplate).send(eq("welcome.dlx.ex"), eq("welcome.dlq.key"), same(message));
        verify(channel).basicAck(22L, false);
    }
}
