package com.example.jiexi;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.example.jiexi.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@SpringBootTest
class EsApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        User user = new User("zhangsan", "男", 18);
        String jsonString = JSONUtil.toJsonStr(user);
        indexRequest.source(jsonString, XContentType.JSON);

        //执行操作
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //提取有用的操作数据
        System.out.println(index);
    }

    @Data
    static class User{
        private String userName;
        private String gender;
        private Integer age;

        public User(String userName, String gender, Integer age) {
            this.userName = userName;
            this.gender = gender;
            this.age = age;
        }
    }

}



