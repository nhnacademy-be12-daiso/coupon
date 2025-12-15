package com.nhnacademy.coupon.global.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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

    @Value("${rabbitmq.dlx.name}")
    private String dlxName;

    @Value("${rabbitmq.dlq.name}")
    private String dlqName;

    @Value("${rabbitmq.dlq-routing.key}")
    private String dlqRoutingKey;


    // 큐 생성 (메시지 담을 통)
    @Bean
    public Queue queue(){
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(dlxName) // 실패 메시지를 보내는 출구
                .deadLetterRoutingKey(dlqRoutingKey) // 실패 메시지가 실제로 쌓이는 보관함
                .build();
    }

    // Exchange 생성 (User 서버와 이름이 같아야 함, 우체국)
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchangeName);
    }
    // "이 'exchange'(우체국)에"
    // "'routingKey'(수신인: team3.coupon.welcome)라고 적힌 편지가 오면"
    // "저 'queue'(우편함: team3.coupon.welcome.queue)로 보내주세요!"
    // 라는 규칙(Binding)을 생성해서 RabbitMQ에 등록함.
    // 바인딩 (큐와 익스체인지를 라우팅 키로 연결)
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
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
        return QueueBuilder.durable(COUPON_QUEUE)
                .withArgument("x-dead-letter-exchange", "team3.coupon.dlx") // DLQ 설정
                .withArgument("x-dead-letter-routing-key", "fail.book")
                .build();
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
