package com.nhnacademy.coupon.global.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponEventPublisher {

    private final AmqpTemplate rabbitTemplate;

    private static final String COUPON_EXCHANGE = "team3.saga.coupon.exchange";
    private static final String ROUTING_KEY_USED = "coupon.used";

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


}
