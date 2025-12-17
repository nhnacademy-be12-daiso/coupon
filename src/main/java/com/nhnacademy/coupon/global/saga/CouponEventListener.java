package com.nhnacademy.coupon.global.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.coupon.domain.coupon.entity.saga.CouponDeduplicationLog;
import com.nhnacademy.coupon.domain.coupon.entity.saga.CouponOutbox;
import com.nhnacademy.coupon.domain.coupon.exception.CouponUpdateFailedException;
import com.nhnacademy.coupon.domain.coupon.exception.FailedSerializationException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponDeduplicationRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponEventListener {

    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final CouponOutboxRepository couponOutboxRepository;
    private final CouponDeduplicationRepository couponDeduplicationRepository;

    @Value("${rabbitmq.routing.used}")
    private String routingKey;

    @RabbitListener(queues = "${rabbitmq.queue.coupon}")
    @Transactional
    public void handleUserDeductedEvent(OrderConfirmedEvent event) {
        log.info("[Coupon API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Coupon API] Order ID : {}", event.getOrderId());

        Long msgId = event.getOrderId();
        if(couponDeduplicationRepository.existsById(msgId)) {
            log.warn("[User API] 중복 이벤트 수신 및 무시 : {}", msgId);
            return;
        }

        try {
            // TODO 포인트 차감 로직

            CouponDeduplicationLog logEntry = new CouponDeduplicationLog(msgId);
            couponDeduplicationRepository.save(logEntry);

            try {
                CouponOutbox outbox = new CouponOutbox(
                    event.getOrderId(),
                    "COUPON",
                    "team3.saga.coupon.exchange",
                        routingKey,
                        objectMapper.writeValueAsString(event)
                );

                couponOutboxRepository.save(outbox);
                publisher.publishEvent(new CouponOutboxCommittedEvent(this, outbox.getId()));
                // 커밋 이벤트 발행


            } catch(FailedSerializationException e) {
                log.warn("객체 직렬화 실패");
                throw new FailedSerializationException("Failed to serialize event payload");
            }

            log.info("[Coupon API] 쿠폰 사용 내역 업데이트 성공");
        } catch(CouponUpdateFailedException e) { // 커스텀 예외 처리 꼭 하기
            log.error("[Coupon API] ===== 쿠폰 사용 내역 업데이트 실패로 인한 보상 트랜잭션 시작 =====");
            log.error("[Coupon API] Order ID : {}", event.getOrderId());

            throw e; // 트랜잭션이 걸려있으므로 예외를 던지면 DB 트랜잭션 롤백
        }
        catch(Exception e) {
            log.error("[Coupon API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
            // DLQ 처리
        }
    }




}
