package com.nhnacademy.coupon.global.saga;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CouponOutboxCommittedEvent extends ApplicationEvent {
    private final Long outboxId;

    public CouponOutboxCommittedEvent(Object source, Long outboxId) {
        super(source);
        this.outboxId = outboxId;
    }
}

