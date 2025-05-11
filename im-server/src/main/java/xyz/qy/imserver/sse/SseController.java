package xyz.qy.imserver.sse;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.qy.imcommon.model.IMSessionInfo;
import xyz.qy.imcommon.util.JwtUtil;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {

    // 建立连接时携带用户标识（例如通过Token解析）
    @GetMapping(path = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestHeader("token") String token) {
        // 解析Token获取用户ID（需实现具体逻辑）
        String strInfo = JwtUtil.getInfo(token);
        IMSessionInfo sessionInfo = JSON.parseObject(strInfo, IMSessionInfo.class);
        log.info("sse heart beat, userId:{}, terminal:{}", sessionInfo.getUserId(), sessionInfo.getTerminal());
        SseEmitter emitter = new SseEmitter(60_000L);
        SseEmitterManager.addEmitter(sessionInfo.getUserId() + "-" + sessionInfo.getTerminal(), emitter);

        return emitter;
    }
}
