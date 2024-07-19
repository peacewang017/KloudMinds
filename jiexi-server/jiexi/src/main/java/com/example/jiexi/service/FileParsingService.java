package com.example.jiexi.service;

import cn.hutool.core.io.FileUtil;
import com.example.jiexi.entity.FileUploadMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.Channel;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileParsingService{

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private Jackson2JsonMessageConverter messageConverter;

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    @RabbitListener(queues = "file-upload-queue", ackMode = "MANUAL")
    public void handleFileUpload(@Payload Map<String, Object> message, Message amqpMessage, Channel channel) {
        try {
            String bucketName = (String) message.get("bucketName");
            String fileName = (String) message.get("fileName");
            String userId = (String) message.get("userId");

            System.out.println("Received message: " + message);

            try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(fileName).build())) {
                String contentText = parseFileContent(fileName, inputStream);

                System.out.println("Extracted content: " + contentText);
                System.out.println(fileName + " parsed successfully");

                // Index parsed content to Elasticsearch
                indexParsedContent(bucketName, fileName, contentText);

                // Upload content to RAG module
                uploadToWeaviate(bucketName, fileName, contentText);

                // Manually acknowledge message
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                System.out.println("File parsing error");
                e.printStackTrace();

                // Handle message rejection if necessary
                channel.basicNack(amqpMessage.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (Exception e) {
            System.out.println("Error processing message");
            e.printStackTrace();
        }
    }

    private String parseFileContent(String fileName, InputStream inputStream) throws IOException {
        String extName = FileUtil.extName(fileName);
        String contentText = "";

        // Parse file content
        switch (extName.toLowerCase()) {
            case "pdf":
                PDDocument document = PDDocument.load(inputStream);
                PDFTextStripper stripper = new PDFTextStripper();
                contentText = stripper.getText(document);
                document.close();
                break;
            case "doc":
                HWPFDocument docDocument = new HWPFDocument(inputStream);
                WordExtractor extractor = new WordExtractor(docDocument);
                contentText = extractor.getText();
                docDocument.close();
                break;
            case "txt":
                contentText = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                break;
            case "docx":
                XWPFDocument docxDocument = new XWPFDocument(inputStream);
                XWPFWordExtractor xwpfExtractor = new XWPFWordExtractor(docxDocument);
                contentText = xwpfExtractor.getText();
                docxDocument.close();
                break;
            default:
                // Handle other file types if necessary
                break;
        }

        return contentText;
    }

    private void indexParsedContent(String bucketName, String fileName, String content) {
        try {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("bucketName", bucketName);
            jsonMap.put("fileName", fileName);
            jsonMap.put("content", content);

            IndexRequest indexRequest = new IndexRequest("files").source(jsonMap, XContentType.JSON);
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

            System.out.println("Indexed document: " + indexResponse.getId());
        } catch (Exception e) {
            System.out.println("Error indexing document");
            e.printStackTrace();
        }
    }

    private void uploadToWeaviate(String bucketName, String fileName, String content) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("bucketname", bucketName);
            data.put("filename", fileName);
            data.put("content", content);

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(flaskServerUrl + "/rag_upload"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(data)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Upload successful for " + fileName + ".");
            } else {
                System.out.println("Upload failed for " + fileName + ".");
            }
        } catch (Exception e) {
            System.out.println("Error uploading to Weaviate");
            e.printStackTrace();
        }
    }
}
