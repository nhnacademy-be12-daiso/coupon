package com.nhnacademy.coupon.global.saga.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.coupon.global.saga.SagaHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundEvent implements SagaEvent {
    @JsonProperty("eventId")
    private String eventId;
    private Long orderId; // orderDetail이여도 됨
    private Long userId;
    private Long outboxId;

    private Long bookId; // 다시 채워넣을 BookId
    private Long quantity; // 책 권수
    private Long refundAmount; // 반품 처리 될 금액 (반품은 포인트로 적립됨)

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public void accept(SagaHandler handler) {
        handler.handleEvent(this);
    }

    /**
     * 반품 시에는 사용 쿠폰, 사용 포인트 전부 사라지는걸 전제로 함
     */
}