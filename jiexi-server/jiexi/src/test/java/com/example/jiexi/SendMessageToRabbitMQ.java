package com.example.jiexi;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class SendMessageToRabbitMQ {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendTestMessage() {
        Map<String, String> message = new HashMap<>();
        message.put("bucketName", "mybucket2");
        message.put("fileName", "test.txt");
        message.put("userId", "2");

        rabbitTemplate.convertAndSend("file-upload-exchange", "file.uploaded", message);
    }
}
