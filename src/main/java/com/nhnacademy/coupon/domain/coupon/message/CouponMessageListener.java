package com.nhnacademy.coupon.domain.coupon.message;

import com.nhnacademy.coupon.domain.coupon.dto.message.CouponIssueMessage;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponMessageListener {

    private final CouponPolicyService couponPolicyService;

    public CouponMessageListener(CouponPolicyService couponPolicyService) {
        this.couponPolicyService = couponPolicyService;
    }

    /**
     * @RabbitListener: 이 메서드는 큐를 계속 감시합니다.
     * queues = "${...}": yml에 적은 큐 이름(team3.coupon.welcome.queue)을 가져옵니다.
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleWelcomeCouponIssue(CouponIssueMessage message){
        log.info("===========================");
        log.info("[RabbitMQ Consumer] 메시지 수신: userId={}", message.userCreatedId());

        try{
            // 서비스 로직 호출 (웰컴 쿠폰 발급)
            couponPolicyService.issueWelcomeCoupon(message.userCreatedId());
            log.info("[RabbitMQ Consumer] 웰컴 쿠폰 발급 완료! userId={}", message.userCreatedId());
        } catch (Exception e){
            log.error("[RabbitMQ Consumer] 쿠폰 발급 실패: userId={}, error={}", message.userCreatedId(), e.getMessage());
        }
        log.info("===========================");
    }
}
