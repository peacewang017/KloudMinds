package com.example.jiexi;

import com.example.jiexi.service.FileParsingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest
public class FileParsingServiceTest {

    @InjectMocks
    private FileParsingService fileParsingService;

    @Mock
    private HttpClient httpClient;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    private final String flaskServerUrl = "http://localhost:5001";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockServer = MockRestServiceServer.createServer(new RestTemplate());
    }

    @Test
    public void testUploadToWeaviate() throws Exception {
        String bucketName = "bucket1";
        String fileName = "file1.txt";
        String content = "This is a test content";

        // Create expected request body
        String expectedRequestBody = objectMapper.writeValueAsString(Map.of(
                "bucketname", bucketName,
                "filename", fileName,
                "content", content
        ));

        // Configure mock server to expect the request
        mockServer.expect(requestTo(new URI(flaskServerUrl + "/rag_upload")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(expectedRequestBody))
                .andRespond(withStatus(OK));

        // Call the method to test
        //fileParsingService.uploadToWeaviate(bucketName, fileName, content);

        // Verify that the request was made as expected
        mockServer.verify();
    }
}
