package com.nhnacademy.coupon.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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

    // 큐 생성 (메시지 담을 통)
    @Bean
    public Queue queue(){
        return new Queue(queueName, true); // 서버가 재시작해도 큐가 사라지지 않음
    }

    // Exchange 생성 (User 서버와 이름이 같아야 함, 우체국)
    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(exchangeName);
    }

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

}
