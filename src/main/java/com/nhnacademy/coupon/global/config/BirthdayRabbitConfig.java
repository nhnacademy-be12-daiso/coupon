package com.nhnacademy.coupon.global.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BirthdayRabbitConfig {

    @Value("${rabbitmq.birthday.exchange}")
    private String birthdayExchange;

    @Value("${rabbitmq.birthday.routing-key}")
    private String birthdayRoutingKey;

    @Value("${rabbitmq.birthday.queue}")
    private String birthdayQueue;

    @Value("${rabbitmq.birthday.retry.exchange}")
    private String retryExchange;

    @Value("${rabbitmq.birthday.retry.routing-key}")
    private String retryRoutingKey;

    @Value("${rabbitmq.birthday.retry.queue}")
    private String retryQueue;

    @Value("${rabbitmq.birthday.retry.delay-ms}")
    private Integer retryDelayMs;

    @Value("${rabbitmq.birthday.dlx}")
    private String dlxName;

    @Value("${rabbitmq.birthday.dlq}")
    private String dlqName;

    @Value("${rabbitmq.birthday.dlq-routing-key}")
    private String dlqRoutingKey;

    // 메인 Exchange
    @Bean
    public TopicExchange birthdayExchange() {
        return new TopicExchange(birthdayExchange);
    }

    // 메인 Queue
    @Bean
    public Queue birthdayQueue() {
        return QueueBuilder.durable(birthdayQueue).build();
    }

    @Bean
    public Binding birthdayBinding(Queue birthdayQueue, TopicExchange birthdayExchange) {
        return BindingBuilder.bind(birthdayQueue)
                .to(birthdayExchange)
                .with(birthdayRoutingKey);
    }

    // Retry Exchange & Queue (10초 지연)
    @Bean
    public DirectExchange birthdayRetryExchange() {
        return new DirectExchange(retryExchange);
    }

    @Bean
    public Queue birthdayRetryQueue() {
        return QueueBuilder.durable(retryQueue)
                .ttl(retryDelayMs)  // 10초 대기
                .deadLetterExchange(birthdayExchange)  // 시간 지나면 다시 메인 큐로
                .deadLetterRoutingKey(birthdayRoutingKey)
                .build();
    }

    @Bean
    public Binding birthdayRetryBinding(
            Queue birthdayRetryQueue,
            DirectExchange birthdayRetryExchange) {
        return BindingBuilder.bind(birthdayRetryQueue)
                .to(birthdayRetryExchange)
                .with(retryRoutingKey);
    }

    // DLX & DLQ (최종 실패)
    @Bean
    public DirectExchange birthdayDlx() {
        return new DirectExchange(dlxName);
    }

    @Bean
    public Queue birthdayDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding birthdayDlqBinding(Queue birthdayDlq, DirectExchange birthdayDlx) {
        return BindingBuilder.bind(birthdayDlq)
                .to(birthdayDlx)
                .with(dlqRoutingKey);
    }

}