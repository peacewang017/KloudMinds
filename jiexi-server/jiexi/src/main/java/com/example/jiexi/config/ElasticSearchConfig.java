package com.example.jiexi.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Value("${spring.data.elasticsearch.client.rest.uris}")
    private String elasticsearchUri;

    @Bean
    public RestHighLevelClient client() {
        HttpHost httpHost = HttpHost.create(elasticsearchUri);
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}