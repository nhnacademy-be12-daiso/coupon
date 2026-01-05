package com.nhnacademy.coupon.domain.coupon.message;


import com.nhnacademy.coupon.global.notify.DoorayNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayCouponDlqListener {

    private final DoorayNotifier doorayNotifier; // Dooray 알림 서비스(웹훅)

    @RabbitListener(queues = "${rabbitmq.birthday.dlq}")
    public void handleDlq(Message message) {
        String correlationId = message.getMessageProperties().getCorrelationId(); // batchId로 쓰고 있지?
        Object retryCount = message.getMessageProperties().getHeaders().get("x-retry-count");

        log.error("[DLQ] Birthday coupon message arrived. correlationId={}, retry={}", correlationId, retryCount);

        doorayNotifier.send(
                "🚨 생일쿠폰 발급 DLQ 적재\n" +
                        "- batchId(correlationId): " + correlationId + "\n" +
                        "- retryCount: " + retryCount + "\n" +
                        "- headers: " + message.getMessageProperties().getHeaders()
        );
    }
}

