package com.nhnacademy.coupon.global.saga.event;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nhnacademy.coupon.global.saga.SagaHandler;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // JSON에 "type": "CONFIRMED" 이런 식으로 정보가 붙어
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderConfirmedEvent.class, name = "CONFIRMED"),
        @JsonSubTypes.Type(value = OrderRefundEvent.class, name = "REFUND"),
        @JsonSubTypes.Type(value = OrderCompensateEvent.class, name = "COMPENSATE")
})
public interface SagaEvent {

    @JsonProperty("eventId")
    String getEventId();
    Long getOrderId();
    void accept(SagaHandler handler);
}
