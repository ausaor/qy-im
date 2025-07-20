package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IAiChatMessageService;
import xyz.qy.implatform.service.IAiChatSessionService;
import xyz.qy.implatform.vo.AiChatMessageVO;
import xyz.qy.implatform.vo.AiChatSessionVO;

import java.util.List;

@Api(tags = "AI会话")
@RequestMapping("/ai")
@RestController
@RequiredArgsConstructor
public class AiChatSessionController {

    private final IAiChatSessionService aiChatSessionService;

    private final IAiChatMessageService aiChatMessageService;

    @ApiOperation(value = "查询会话列表", notes = "查询会话列表")
    @GetMapping("/session/list")
    public Result<List<AiChatSessionVO>> listSessions() {
        return ResultUtils.success(aiChatSessionService.listSessions());
    }

    @ApiOperation(value = "查询会话消息列表", notes = "查询会话消息列表")
    @GetMapping("/session/message/list")
    public Result<List<AiChatMessageVO>> listMessages(@RequestParam Long sessionId) {
        return ResultUtils.success(aiChatMessageService.listMessages(sessionId));
    }
}
