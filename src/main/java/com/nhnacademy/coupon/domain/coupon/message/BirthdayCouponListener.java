package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BirthdayCouponListener {

    private final CouponPolicyService couponPolicyService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.birthday.retry.exchange}")
    private String retryExchange;

    @Value("${rabbitmq.birthday.retry.routing-key}")
    private String retryRoutingKey;

    @Value("${rabbitmq.birthday.dlx}")
    private String dlxExchange;

    @Value("${rabbitmq.birthday.dlq-routing-key}")
    private String dlqRoutingKey;

    private static final int MAX_RETRY = 3;

    @RabbitListener(queues = "${rabbitmq.birthday.queue}")
    public void handleBirthdayCouponEvent(
            BirthdayCouponBulkEvent event,
            Message message,
            Channel channel) throws Exception {

        long tag = message.getMessageProperties().getDeliveryTag();
        int retryCount = (int) message.getMessageProperties()
                .getHeaders()
                .getOrDefault("x-retry-count", 0);

        try {
            log.info("[Coupon] bulk birthday event: batchId={}, size={}, retry={}",
                    event.batchId(), event.userIds().size(), retryCount);

            couponPolicyService.issueBirthdayCouponsBulk(event.userIds());

            // 성공 -> ACK
            channel.basicAck(tag, false);
            log.info("[Coupon] bulk 발급 성공: batchId={}, size={}",
                    event.batchId(), event.userIds().size());

        } catch (Exception e) {
            log.error("[Coupon] bulk issue failed: batchId={}, size={}, retry={}, error={}",
                    event.batchId(), event.userIds().size(), retryCount, e.getMessage());

            int nextRetry = retryCount + 1;

            Message retryMessage = MessageBuilder
                    .fromMessage(message)
                    .setHeader("x-retry-count", nextRetry)
                    .build();

            try {
                if (nextRetry <= MAX_RETRY) {
                    rabbitTemplate.send(retryExchange, retryRoutingKey, retryMessage);
                } else {
                    rabbitTemplate.send(dlxExchange, dlqRoutingKey, retryMessage);
                }

                // 재발행이 성공했으면 원본 ACK
                channel.basicAck(tag, false);

            } catch (Exception publishFail) {
                // 재발행 실패면 원본을 살려두는게 안전 (requeue)
                log.error("[Coupon] republish failed; requeue original. error={}", publishFail.getMessage());
                channel.basicNack(tag, false, true);
            }
        }
    }
}
