package com.nhnacademy.coupon.global.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class CouponOutboxRelayManager {

    private final CouponOutboxRelayProcessor couponOutboxRelayProcessor;

    // PaymentEventListener가 커밋된 이후 실행됨
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCommitted(CouponOutboxCommittedEvent event) {
        couponOutboxRelayProcessor.processRelay(event.getOutboxId());
    }
}