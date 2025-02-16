package com.qorvia.paymentservice.config;

import com.qorvia.paymentservice.utils.AppConstants;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
@EnableRabbit
public class RabbitMQConfig {


    // --- Payment Service RPC Queue, Exchange, and Routing ---

    @Bean
    public Queue paymentServiceRpcQueue() {
        return new Queue(AppConstants.PAYMENT_SERVICE_RPC_QUEUE, true);
    }

    @Bean
    public Exchange paymentServiceRpcExchange() {
        return new DirectExchange(AppConstants.PAYMENT_SERVICE_RPC_EXCHANGE, true, false);
    }

    @Bean
    public Binding paymentServiceRpcBinding() {
        return BindingBuilder
                .bind(paymentServiceRpcQueue())
                .to(paymentServiceRpcExchange())
                .with(AppConstants.PAYMENT_SERVICE_RPC_ROUTING_KEY)
                .noargs();
    }


    // Configure the RPC Listener Container for the RPC queues
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(3);
        return factory;
    }
}