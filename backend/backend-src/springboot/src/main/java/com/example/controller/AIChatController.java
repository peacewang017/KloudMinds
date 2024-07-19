package com.example.controller;

import com.example.entity.Account;
import com.example.service.AIChatService;
import com.example.utils.TokenUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/aiChat")
@Api(tags = "Chat操作接口")
public class AIChatController {
    @Resource
    private AIChatService aiChatService;

    @ApiOperation("单轮聊天")
    @GetMapping("/chat/{docNames}")
    public String chat(@PathVariable List<String> docNames, @RequestParam String msg) {
        return aiChatService.chat(docNames, msg);
    }

    @ApiOperation("文档总结")
    @GetMapping("/doc/{docNames}")
    public String doc(@PathVariable List<String> docNames) {
        return aiChatService.doc(docNames);
    }
}

