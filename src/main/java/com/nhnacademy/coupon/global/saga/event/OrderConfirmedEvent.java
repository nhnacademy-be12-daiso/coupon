package com.nhnacademy.coupon.global.saga.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhnacademy.coupon.global.saga.SagaHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 분산된 로컬 트랜잭션을 수행하기 위해 필요한 '확정된 최종 데이터'
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent implements SagaEvent {
    @JsonProperty("eventId")
    private String eventId;
    private Long orderId;
    private Long userId;
    private Long outboxId;

    // 여기 있는건 이미 다 검증이 됐음을 전제로 한다
    private Map<Long, Integer> bookList; // bookId, quantity
    private Long totalAmount;
    private Long usedPoint; // 사용 포인트
    private Long savedPoint; // 적립 포인트
    private List<Long> usedCouponIds;

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public void accept(SagaHandler handler) {
        handler.handleEvent(this);
    }
}
