package com.example.service;

import com.example.entity.Account;
import com.example.utils.TokenUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIChatService {

    @Value("${flask.server.url}")
    private String flaskServerUrl;

    @Value("${ai.chat.server.url}")
    private String aiChatServerUrl;


    public String chat(List<String> docNames, String msg) {
        try {
            Account currentUser = TokenUtils.getCurrentUser();
            String bucketName = "bucket" + currentUser.getId();

            // Step 1: 从 Weaviate 向量数据库中检索相关文件名和内容
            Map<String, List<String>> weaviateResults = searchInWeaviate(bucketName, msg);
            System.out.println("Weaviate Results: " + weaviateResults);

            // Step 2: 过滤出在 docNames 中的文件内容
            StringBuilder contentBuilder = new StringBuilder();
            int chunkNumber = 1;
            for (Map.Entry<String, List<String>> entry : weaviateResults.entrySet()) {
                if (docNames.contains(entry.getKey())) {
                    for (String content : entry.getValue()) {
                        contentBuilder.append("the ").append(chunkNumber).append(" chunk: ").append(content).append("\n");
                        chunkNumber++;
                    }
                }
            }
            String content = contentBuilder.toString();
            System.out.println(content);
            //包装prompt,使其根据content的文档内容回答问题
            String prompt = "Answer questions using the materials provided above." + "The question is" + msg;

            System.out.println("Prompt: " + prompt);

            // Step 3: 调用 AI 聊天服务器并返回结果
            if (!content.isEmpty()) {
                String chatResponse = callAiChatServer(content, prompt);
                System.out.println("Chat Response: " + chatResponse);
                return chatResponse;
            } else {
                System.out.println("No relevant documents found in the provided list.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "No response";
    }

    private Map<String, List<String>> searchInWeaviate(String bucketName, String keyword) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(flaskServerUrl + "/rag_search");
        httpPost.setHeader("Content-Type", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("bucketname", bucketName);
        data.put("targetcontent", keyword);
        String json = new ObjectMapper().writeValueAsString(data);

        System.out.println("Request JSON: " + json);

        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);

        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity responseEntity = response.getEntity();

        Map<String, List<String>> resultMap = new HashMap<>();
        if (response.getStatusLine().getStatusCode() == 200 && responseEntity != null) {
            String responseBody = EntityUtils.toString(responseEntity);
            List<Map<String, String>> articles = new ObjectMapper().readValue(responseBody, List.class);

            for (Map<String, String> article : articles) {
                String fileName = article.get("filename");
                String content = article.get("content");
                resultMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(content);
            }
        } else {
            throw new RuntimeException("Search in Weaviate failed with status code: " + response.getStatusLine().getStatusCode());
        }

        return resultMap;
    }


    private String callAiChatServer(String content, String prompt) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(aiChatServerUrl + "/request");
        httpPost.setHeader("Content-Type", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("content", content);
        data.put("prompt", prompt);
        String json = new ObjectMapper().writeValueAsString(data);

        System.out.println("Request JSON: " + json);
        System.out.println("Request URL: " + aiChatServerUrl + "/request");

        StringEntity entity = new StringEntity(json, "UTF-8");
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 200 && responseEntity != null) {
                String responseString = EntityUtils.toString(responseEntity);
                // 解析JSON响应以提取ai_answer字段
                JsonNode jsonResponse = new ObjectMapper().readTree(responseString);
                if (jsonResponse.has("ai_answer")) {
                    return jsonResponse.get("ai_answer").asText();
                } else {
                    throw new RuntimeException("AI chat server response does not contain 'ai_answer' field.");
                }
            } else {
                String errorMessage = responseEntity != null ? EntityUtils.toString(responseEntity) : "No response entity";
                throw new RuntimeException("AI chat server request failed with status code: " + response.getStatusLine().getStatusCode() + " and response: " + errorMessage);
            }
        } catch (IOException e) {
            System.err.println("IOException while calling AI chat server: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected exception while calling AI chat server: " + e.getMessage());
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }


    @Autowired
    private RestHighLevelClient client;

    /**
     * 文档总结
     *
     * @param docNames
     * @return
     */
    public String doc(List<String> docNames) {
        Account currentUser = TokenUtils.getCurrentUser();
        String bucketName = "bucket" + currentUser.getId();

        SearchRequest searchRequest = new SearchRequest("files");
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("bucketName.keyword", bucketName))
                .filter(QueryBuilders.termsQuery("fileName.keyword", docNames));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        List<String> contentList = new ArrayList<>();
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String content = (String) hit.getSourceAsMap().get("content");
                contentList.add(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (contentList.isEmpty()) {
            return "No relevant documents found.";
        }

        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < contentList.size(); i++) {
            // 在每个文档前添加分隔行
            contentBuilder.append("----------\n");
            contentBuilder.append("Document ").append(i + 1).append(":\n");
            contentBuilder.append(contentList.get(i)).append("\n");
            // 在每个文档后也添加分隔行，如果是最后一个文档则可以省略
            if (i < contentList.size() - 1) {
                contentBuilder.append("----------\n");
            }
        }
            // 如果需要，在所有文档结束后添加一个总的分隔行
        contentBuilder.append("----------\n");

        String content = contentBuilder.toString();
        System.out.println("Content: " + content);
        try {
            return callAiChatServer(content, "Generate comprehensive summaries for multiple articles, highlighting key events, important figures, and clearly identifying the final outcome or conclusion.\n");
        } catch (IOException e) {
            e.printStackTrace();
            return "Error while calling AI chat server.";
        }
    }
}
