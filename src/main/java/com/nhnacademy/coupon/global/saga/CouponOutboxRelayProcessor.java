package com.nhnacademy.coupon.global.saga;

import com.nhnacademy.coupon.domain.coupon.entity.saga.CouponOutbox;
import com.nhnacademy.coupon.domain.coupon.exception.ExternalServiceException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponOutboxRelayProcessor {

    private final CouponEventPublisher couponEventPublisher;
    private final CouponOutboxRepository couponOutboxRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRelay(Long outboxId) {

        CouponOutbox outbox = couponOutboxRepository.findById(outboxId).orElseThrow();
        // <<<<<< 예외처리

        try {
            couponEventPublisher.publishCouponOutboxMessage(
                    outbox.getTopic(),
                    outbox.getRoutingKey(),
                    outbox.getPayload()
            );
            outbox.markAsPublished();
            couponOutboxRepository.save(outbox);

        } catch (ExternalServiceException e) { // 실패시 재시도 및 롤백
            if (outbox.getRetryCount() < 3) {
                outbox.incrementRetryCount();
                couponOutboxRepository.save(outbox); // DB에 업데이트
            } else {
                outbox.markAsFailed();
                couponOutboxRepository.save(outbox); // DB에 업데이트
                log.error("[Coupon API] Outbox 메세지 최종 발행 실패 OutboxID : {}", outboxId);
            }
            throw e; // 예외 던져서 롤백 유도
        }
    }
}