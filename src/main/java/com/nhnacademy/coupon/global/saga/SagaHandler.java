package com.nhnacademy.coupon.global.saga;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponCancelRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.exception.CouponUpdateFailedException;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import com.nhnacademy.coupon.global.saga.event.OrderCompensateEvent;
import com.nhnacademy.coupon.global.saga.event.OrderConfirmedEvent;
import com.nhnacademy.coupon.global.saga.event.SagaEvent;
import com.nhnacademy.coupon.global.saga.event.SagaReply;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SagaHandler {

    private final SagaTestService testService;
    private final SagaReplyService replyService;
    private final UserCouponService userCouponService;

    @Transactional
    public void handleEvent(SagaEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
//            testService.process(); // 일부러 재고 부족 터트리기
            // TODO 01 쿠폰 사용 처리 로직 (이한음 님)
            /**
             *  본인들 서비스 주입받아서 로직 구현하시면 됩니다.
             *  매개변수로 넘어온 event DTO를 까보시면 필요한 정보들이 담겨 있습니다.
             *  그거 토대로 각자 로직에 구현해주면 됨 (재고 차감, 포인트 차감, 쿠폰 사용 처리)
             *
             *
             *  만약 쿠폰 사용 처리 중 오류가 발생한다?
             *  그럼 하단에 CouponUpdateFailedException 던지면 됩니다!
             *
             *  더 좋은 로직 있다면 추천 가능
             */

            if(event instanceof OrderConfirmedEvent confirmedEvent) {
                List<CouponUseRequest> coupons = confirmedEvent.getUsedCouponIds().stream()
                        .map(CouponUseRequest::new)
                        .toList();

                // BatchCouponUseRequest
                BatchCouponUseRequest request = new BatchCouponUseRequest(confirmedEvent.getOrderId(), coupons);

                userCouponService.useCoupons(confirmedEvent.getUserId(), request);

                log.info("[Coupon API] 쿠폰 사용내역 업데이트 성공 - Order : {}", confirmedEvent.getOrderId());
            }

        } catch(CouponUpdateFailedException e) { // 쿠폰 사용내역 업데이트 실패
            // ---> 비즈니스상 실패를 의미함 (쿠폰 유효기간 지났다던가)
            isSuccess = false;
            reason = "UPDATE_FAILED";
            log.error("[Coupon API] 쿠폰 사용내역 업데이트 실패 - Order : {}", event.getOrderId());
            throw e;
        } catch(Exception e) {
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            // 이렇게 예외 범위를 넓게 해놔야 무슨 에러가 터져도 finally 문이 실행됨
            log.error("[Coupon API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
            throw e;
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "COUPON",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_RK);
        }
    }

    @Transactional
    public void handleRollbackEvent(OrderCompensateEvent event) {

        boolean isSuccess = true; // 성공 여부
        String reason = null; // 실패시 사유

        try {
            // TODO 02 쿠폰 '보상' 로직 (이한음 님)
            /**
             * 동일하게 서비스 주입받아서 하시면 되는데,
             * 여기서는 '뭔가 잘못돼서 다시 원복시키는 롤백'의 과정입니다.
             * 그니까 아까 사용 처리 했던 쿠폰을 다시 원복시키는 로직을 구현하시면 됩니다.
             */
            List<Long> usedCouponIds = event.getUsedCouponIds();
            if(usedCouponIds == null || usedCouponIds.isEmpty()){
                log.info("[Coupon API] 보상: 쿠폰 미사용 - orderId={}", event.getOrderId());
                return;
            }
            Long orderId = event.getOrderId();
            String cancelReason = "SAGA_COMPENSATION";

            CouponCancelRequest request = new CouponCancelRequest(orderId, cancelReason, usedCouponIds);
            userCouponService.cancelCouponUsage(event.getUserId(),request);

            log.info("[Coupon API] 쿠폰 사용내용 복구 성공 - Order : {}", event.getOrderId());

        } catch(Exception e) {
            isSuccess = false;
            reason = "SYSTEM_ERROR";
            log.error("[Coupon API] 예상치 못한 시스템 에러 발생 - Order : {}", event.getOrderId(), e);
        }
        finally {
            // 성공했든 실패했든 답장은 해야함
            SagaReply reply = new SagaReply(
                    event.getOrderId(),
                    "COUPON",
                    isSuccess,
                    reason
            );

            // 응답 메시지 전송
            replyService.send(event, reply, SagaTopic.REPLY_COMPENSATION_RK);
        }
    }
}
