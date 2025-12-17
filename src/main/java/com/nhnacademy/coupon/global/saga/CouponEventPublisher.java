package com.nhnacademy.coupon.global.saga;

import com.nhnacademy.coupon.domain.coupon.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    private static final String COUPON_EXCHANGE = "team3.saga.coupon.exchange";
    @Value("${rabbitmq.routing.used}")
    private String ROUTING_KEY_USED;

    // 로컬 트랜잭션이 커밋된 후에 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCouponUsedEvent(OrderConfirmedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    COUPON_EXCHANGE,
                    ROUTING_KEY_USED,
                    event
            );
            log.info("[Coupon API] 쿠폰 사용 이벤트 발행 완료 : {}", ROUTING_KEY_USED);

        } catch(Exception e) {
            log.warn("[Coupon API] RabbitMQ 발행 실패 : {}", e.getMessage());
            // TODO : Outbox 패턴 또는 재시도 로직 구현해야함!!!
        }
    }

    public void publishCouponOutboxMessage(String topic, String routingKey, String payload) {

        try {
            byte[] body = payload.getBytes(StandardCharsets.UTF_8);

            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON); // 👈 핵심 수정
            properties.setContentEncoding("UTF-8");
            Message message = new Message(body);

            rabbitTemplate.send(topic, routingKey, message); // 직렬화 해서 생으로 보냄

            log.info("[Coupon API] 다음 이벤트 발행 완료 : Coupon API -> Payment API");
        } catch (Exception e) {
            log.warn("[Coupon API] RabbitMQ 발행 실패 : {}", e.getMessage());
            throw new ExternalServiceException("rabbitMQ 메세지 발행 실패");
        }
    }


}
