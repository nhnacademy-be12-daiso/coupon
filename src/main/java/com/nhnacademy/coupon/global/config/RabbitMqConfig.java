package com.nhnacademy.coupon.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.retry.exchange.name}")
    private String retryExchangeName;

    @Value("${rabbitmq.retry.routing.key}")
    private String retryRoutingKey;

    @Value("${rabbitmq.retry.queue.name}")
    private String retryQueueName;

    @Value("${rabbitmq.retry.delay-ms}")
    private Integer retryDelayMs;

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;

    @Value("${rabbitmq.dlq.name}")
    private String dlqName;

    @Value("${rabbitmq.dlq-routing.key}")
    private String dlqRoutingKey;

    // 1) 본 Exchange (Producer가 보내는 곳)
    @Bean(name = "couponMainExchange")
    public TopicExchange couponMainExchange() {
        return new TopicExchange(exchangeName);
    }

    // 본 큐(v2) - Producer routingKey(team3.coupon.welcome)로 바인딩
    @Bean(name = "welcomeQueueV2")
    public Queue welcomeQueueV2() {
        return QueueBuilder.durable(queueName).build();
    }
    // "이 'exchange'(우체국)에"
    // "'routingKey'(수신인: team3.coupon.welcome)라고 적힌 편지가 오면"
    // "저 'queue'(우편함: team3.coupon.welcome.queue)로 보내주세요!"
    // 라는 규칙(Binding)을 생성해서 RabbitMQ에 등록함.
    // 바인딩 (큐와 익스체인지를 라우팅 키로 연결)

    // 기존 routingKey(team3.coupon.welcome)로 v2 큐에 바인딩
    @Bean
    public Binding bindWelcomeQueue(
            @Qualifier("welcomeQueueV2") Queue q,
            @Qualifier("couponMainExchange") TopicExchange ex) {
        return BindingBuilder.bind(q).to(ex).with(routingKey);
    }

    // 2) retry exchange/queue(10초 지연)
    @Bean(name = "retryExchange")
    public DirectExchange retryExchange() {
        return new DirectExchange(retryExchangeName);
    }

    @Bean(name = "retryQueue")
    public Queue retryQueue() {
        // retryQueue에 들어오면 10초 뒤 (DLX 기능)로 원래 exchange+routingKey
        return QueueBuilder.durable(retryQueueName)
                .ttl(retryDelayMs) // 실패하면 10초동안 묶어두고
                .deadLetterExchange(exchangeName) // TTL 만료되면
                .deadLetterRoutingKey(routingKey)  // welcome.queue.v2 원래 큐로 보내기
                .build();
    }

    @Bean
    public Binding bindRetryQueue(
            @Qualifier("retryQueue") Queue q,
            @Qualifier("retryExchange") DirectExchange ex) {
        return BindingBuilder.bind(q).to(ex).with(retryRoutingKey);
    }

    // 최종 DLX/DLQ, 한번만 만든다.
    @Bean(name = "finalDlx")
    public DirectExchange finalDlx() {
        return new DirectExchange(dlxName);
    }

    @Bean(name = "finalDlq")
    public Queue finalDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding bindFinalDlq(
            @Qualifier("finalDlq") Queue q,
            @Qualifier("finalDlx") DirectExchange ex) {
        return BindingBuilder.bind(q).to(ex).with(dlqRoutingKey);
    }

        // 메시지 변환기 (JSON -> 객체)
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();

    }

    // RabbitTemplate (메시지 보낼 때 필요하지만, 받는 쪽에서도 변환기 설정을 위해 필요)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ----- saga 설정 ------

    private static final String USER_EXCHANGE = "team3.user.exchange";
    @Value("${rabbitmq.queue.coupon}")
    private String COUPON_QUEUE;
    private static final String ROUTING_KEY_DEDUCTED = "point.deducted";

    private static final String COUPON_EXCHANGE = "team3.coupon.exchange";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue couponQueue() {
        return new Queue(COUPON_QUEUE, true);
    }

    @Bean
    public Binding bindingUserDeducted(Queue couponQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(couponQueue)
                .to(userExchange)
                .with(ROUTING_KEY_DEDUCTED);
    }

    @Bean
    public TopicExchange couponExchange() {
        return new TopicExchange(COUPON_EXCHANGE);
    }


    @Bean
    public DirectExchange welcomeDlx() {
        return new DirectExchange(dlxName);
    }

    @Bean(name = "welcomeDlq")
    public Queue welcomeDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding bindWelcomeDlq(Queue welcomeDlq, DirectExchange welcomeDlx) {
        return BindingBuilder.bind(welcomeDlq).to(welcomeDlx).with(dlqRoutingKey);
    }

}
