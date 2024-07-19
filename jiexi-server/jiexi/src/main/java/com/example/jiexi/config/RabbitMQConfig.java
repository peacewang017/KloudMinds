package com.example.jiexi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue fileUploadQueue() {
        return new Queue("file-upload-queue", true);
    }

    @Bean
    public Queue fileDeleteQueue() {
        return new Queue("file-delete-queue", true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("file-upload-exchange");
    }

    @Bean
    public Binding fileUploadBinding(Queue fileUploadQueue, DirectExchange exchange) {
        return BindingBuilder.bind(fileUploadQueue).to(exchange).with("file.uploaded");
    }

    @Bean
    public Binding fileDeleteBinding(Queue fileDeleteQueue, DirectExchange exchange) {
        return BindingBuilder.bind(fileDeleteQueue).to(exchange).with("file.deleted");
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}