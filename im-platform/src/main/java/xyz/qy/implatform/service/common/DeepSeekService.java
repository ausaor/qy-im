package xyz.qy.implatform.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import xyz.qy.implatform.config.DeepSeekConfig;
import xyz.qy.implatform.dto.ChatRequest;
import xyz.qy.implatform.dto.ChatResponse;

import javax.annotation.Resource;

@Service
public class DeepSeekService {

    @Resource(name = "deepSeekRestTemplate")
    private RestTemplate deepSeekRestTemplate;

    @Resource
    private DeepSeekConfig deepSeekConfig;

    public ChatResponse chatCompletion(ChatRequest request) {
        try {
            ResponseEntity<ChatResponse> response = deepSeekRestTemplate.postForEntity(
                    deepSeekConfig.getUrl(),
                    request,
                    ChatResponse.class
            );
            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("调用DeepSeek API失败: " + e.getMessage(), e);
        }
    }
}
