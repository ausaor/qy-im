package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.SysMsgQueryDTO;
import xyz.qy.implatform.dto.SystemMessageDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ISystemMessageService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 系统消息
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Api(tags = "系统消息")
@RequestMapping("/message/system")
@RestController
public class SystemMessageController {
    @Resource
    private ISystemMessageService systemMessageService;

    @ApiOperation(value = "保存系统消息", notes = "保存系统消息")
    @PostMapping("/save")
    public Result save(@Valid @RequestBody SystemMessageDTO dto) {
        systemMessageService.save(dto);
        return ResultUtils.success();
    }

    @GetMapping("/pullOfflineMessage")
    @ApiOperation(value = "拉取离线消息", notes = "拉取离线消息,消息将通过webSocket异步推送")
    public Result pullOfflineMessage(@RequestParam Long minSeqNo) {
        systemMessageService.pullOfflineMessage(minSeqNo);
        return ResultUtils.success();
    }

    @ApiOperation(value = "消息已读",notes="将群聊中的消息状态置为已读")
    @PutMapping("/readed")
    public Result readedMessage(@RequestParam Long pusherId) {
        systemMessageService.readedMessage(pusherId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询系统消息", notes = "分页查询系统消息")
    @PostMapping("/page")
    public Result pageSysMsg(@RequestBody SysMsgQueryDTO queryDTO) {
        return ResultUtils.success(systemMessageService.pageSysMsg(queryDTO));
    }
}
