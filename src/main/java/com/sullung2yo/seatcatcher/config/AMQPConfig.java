package com.sullung2yo.seatcatcher.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class AMQPConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.binding.prefix}")
    private String bindingKey;

    // RabbitMQ에서 Seatcatcher 서비스에 사용할 큐
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName).build();
    }

    // TODO : 좌석 관련 이벤트 작업 실패 시 실패 작업을 넣어둘 DLQ 구현 필요

    // RabbitMQ Exchange 선언
    @Bean
    public TopicExchange topicExchange() {
        // Exchange : 메세지 받았을 때 어떤 큐로 전달할지 결정해주는 역할
        // durable 설정하면 서버가 재시작되더라도 큐가 유지된다.
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    // Binding 선언
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        /*
         * Binding Key를 사용해서 queue와 Exchange를 연결해주고,
         * Routing Key를 사용해서 Binding bean을 생성해주는 메서드
         * 즉, Exchange와 Queue를 연결해주는 역할이라고 이해하면 됨
         */
        return BindingBuilder.bind(queue).to(exchange).with(bindingKey);
    }

    // 응답 반환할 때 사용할 MessageConverter (JSON 직렬화)
    @Bean
    public MessageConverter jacksonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter); // 메세지 어떤 형식으로 변환할건지 지정 (JSON)
        rabbitTemplate.setMandatory(true); // returnCallback을 사용하기 위해 true로 설정 -> returnCallback : 메세지를 전송했을 때, 성공/실패 여부를 알려주는 콜백
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.error("메세지 전송 실패: {}, {}, {}", returnedMessage.getExchange(), returnedMessage.getRoutingKey(), returnedMessage.getMessage());
        });
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("메세지 전송 실패: {}, {}", correlationData, cause);
            }
        });
        return rabbitTemplate;
    }
}
