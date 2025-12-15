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
        log.info("[RabbitMQ Consumer] 메시지 수신: userCreatedId={}", message.userCreatedId());

        try{
            // 서비스 로직 호출 (웰컴 쿠폰 발급)
            couponPolicyService.issueWelcomeCoupon(message.userCreatedId());
            log.info("[RabbitMQ Consumer] 웰컴 쿠폰 발급 완료! userCreatedId={}", message.userCreatedId());
        } catch (Exception e){
            log.error("[RabbitMQ Consumer] 쿠폰 발급 실패: userCreatedId={}, error={}", message.userCreatedId(), e.getMessage());
            throw e; // 이 줄이 DLQ로 보내는 트리거
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
