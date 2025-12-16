package com.nhnacademy.coupon.global.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SagaConfig {
    private static final String USER_EXCHANGE = "team3.saga.user.exchange";
    @Value("${rabbitmq.queue.coupon}")
    private String COUPON_QUEUE;
    private static final String ROUTING_KEY_DEDUCTED = "point.deducted";

    private static final String COUPON_EXCHANGE = "team3.saga.coupon.exchange";

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue couponQueue() {
        return QueueBuilder.durable(COUPON_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.coupon.dlx") // DLQ 설정
                .withArgument("x-dead-letter-routing-key", "fail.coupon")
                .build();
    }

    @Bean
    public Binding bindingUserDeducted(Queue couponQueue, DirectExchange userExchange) {
        return BindingBuilder.bind(couponQueue)
                .to(userExchange)
                .with(ROUTING_KEY_DEDUCTED);
    }

    @Bean
    public DirectExchange couponExchange() {
        return new DirectExchange(COUPON_EXCHANGE);
    }
}
