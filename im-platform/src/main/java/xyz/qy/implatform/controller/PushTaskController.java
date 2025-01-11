package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.PushTaskDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IPushTaskService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 推送任务
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Api(tags = "推送任务")
@RequestMapping("/openApi/push/task")
@RestController
public class PushTaskController {
    @Resource
    private IPushTaskService pushTaskService;

    @ApiOperation(value = "推送系统消息", notes = "推送系统消息")
    @PostMapping("/pushSystemMessage")
    public Result pushSystemMessage(@Valid @RequestBody PushTaskDTO dto) {
        pushTaskService.pushSystemMessage(dto);
        return ResultUtils.success();
    }
}
