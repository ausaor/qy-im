package xyz.qy.implatform.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.ChatRequest;
import xyz.qy.implatform.dto.ChatResponse;
import xyz.qy.implatform.service.common.DeepSeekService;

import javax.annotation.Resource;

/**
 * AI对话
 *
 * @author Polaris
 */
@RequestMapping("/ai/chat")
@RestController
public class AiChatController {
    @Resource
    private DeepSeekService deepSeekService;

    @PostMapping("/deepseek")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return deepSeekService.chatCompletion(request);
    }
}
