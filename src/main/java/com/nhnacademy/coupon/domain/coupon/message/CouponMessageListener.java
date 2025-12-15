package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.dto.message.CouponIssueMessage;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponMessageListener {

    private static final String RETRY_HEADER = "x-retry-count";

    private final CouponPolicyService couponPolicyService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.retry.exchange.name}")
    private String retryExchangeName;

    @Value("${rabbitmq.retry.routing.key}")
    private String retryRoutingKey;

    @Value("${rabbitmq.retry.max-attempts}")
    private int maxAttempts;

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;

    @Value("${rabbitmq.dlq-routing.key}")
    private String dlqRoutingKey;

    /**
     * @RabbitListener: 이 메서드는 큐를 계속 감시합니다.
     * queues = "${...}": yml에 적은 큐 이름(team3.coupon.welcome.queue)을 가져옵니다.
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleWelcomeCouponIssue(CouponIssueMessage payload,
                                         Message message,
                                         Channel channel) throws Exception {
        // deliveryTag: "이 메시지가 몇 번째로 전달됐는지"에 대한 고유 번호, ack 할 때 반드시 필요
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Integer retryCount = (Integer) message.getMessageProperties()
                        .getHeaders()
                        .getOrDefault(RETRY_HEADER, 0);

        try{
            log.info("[Consumer] 수신 userId={}, retryCount={}", payload.userCreatedId(), retryCount);

            couponPolicyService.issueWelcomeCoupon(payload.userCreatedId());

            // 성공 -> 정상 ack(이 메시지를 잘 처리했다는 사인 같은거)
            channel.basicAck(deliveryTag, false);
        } catch (Exception e){
            int nextRetryCount = retryCount + 1;
            message.getMessageProperties().getHeaders().put(RETRY_HEADER, nextRetryCount);
            if (nextRetryCount <= maxAttempts) {
                // retry (10초 지연 큐로)
                log.warn("[Consumer] retry {} / userId={}", nextRetryCount, payload.userCreatedId());
                rabbitTemplate.send(retryExchangeName,retryRoutingKey,message);
            } else{
                // 최종 DLQ
                log.error("[Consumer] DLQ 전송 userId={}", payload.userCreatedId());

                rabbitTemplate.send(dlxName,dlqRoutingKey,message);
            }
            // 내가 책임지고 retry든 DLQ든 이미 옮겨놨ㄴ으니까 원본 메시지는 이제 필요 없어! 라는 코드
            channel.basicAck(deliveryTag, false);

        }
    }
//    @RabbitListener(queues = "${rabbitmq.queue.name}")
//    public void handleWelcomeCouponIssue(CouponIssueMessage message){
//        log.info("[Consumer] received userId={}", message.userCreatedId());
//
//        // 테스트: 특정 userId면 일부러 실패
//        if (message.userCreatedId() == 999L) {
//            throw new RuntimeException("TEST FAIL: forced error");
//        }
//
//        couponPolicyService.issueWelcomeCoupon(message.userCreatedId());
//    }
}
