package com.nhnacademy.coupon.global.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponEventListener {

    private final CouponEventPublisher couponEventPublisher;


    @RabbitListener(queues = "${rabbitmq.queue.coupon}")
    @Transactional
    public void handleUserDeductedEvent(OrderConfirmedEvent event) {
        log.info("[Coupon API] ===== 주문 확정 이벤트 수신됨 =====");
        log.info("[Coupon API] Order ID : {}", event.getOrderId());

        try {
            // TODO 포인트 차감 로직

            // saga 다음 단계를 위한 이벤트 발행
            couponEventPublisher.publishCouponUsedEvent(event);

            log.info("[Coupon API] 쿠폰 사용 내역 업데이트 성공");
            log.info("[Coupon API] 다음 이벤트 발행 완료 : Coupon API -> Payment API");
        } catch(Exception e) { // 커스텀 예외 처리 꼭 하기
            log.error("[Coupon API] ===== 쿠폰 사용 내역 업데이트 실패로 인한 보상 트랜잭션 시작 =====");
            log.error("[Coupon API] Order ID : {}", event.getOrderId());

            throw e; // 트랜잭션이 걸려있으므로 예외를 던지면 DB 트랜잭션 롤백
        }
//        catch(Exception e) {
//            log.error("[Coupon API] 이벤트 처리 중 예상치 못한 오류 발생 : {}", e.getMessage());
//             TODO Dead Letter Queue 처리
//             ---> 근데 여기서도 보상 트랜잭션 날려야하는거 아님?
//        }
    }




}
