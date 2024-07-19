package com.example.jiexi.service;

import com.example.jiexi.entity.FileUploadMessage;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileDeletingService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @RabbitListener(queues = "file-delete-queue")
    public void handleFileDelete(FileUploadMessage message) {
        String bucketName = message.getBucketName();
        String fileName = message.getFileName();

        System.out.println("Received delete message for: " + fileName + " in bucket: " + bucketName);

        try {
            // Construct a query to find the document
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery("bucketName", bucketName))
                    .must(QueryBuilders.matchQuery("fileName", fileName));

            // Create a DeleteByQueryRequest
            DeleteByQueryRequest request = new DeleteByQueryRequest("files")
                    .setQuery(queryBuilder);

            // Execute the delete by query request
            BulkByScrollResponse response = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);

            System.out.println("Deleted " + response.getDeleted() + " documents for: " + fileName + " in bucket: " + bucketName);
        } catch (Exception e) {
            System.out.println("Error deleting document from Elasticsearch");
            e.printStackTrace();
        }
    }
}