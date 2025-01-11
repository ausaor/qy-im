package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.imclient.annotation.BanSendMsg;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.vo.GroupMessageVO;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊消息")
@RestController
@RequestMapping("/message/group")
public class GroupMessageController {

    @Autowired
    private IGroupMessageService groupMessageService;

    @BanSendMsg(msgType = "group")
    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息", notes = "发送群聊消息")
    public Result<GroupMessageVO> sendMessage(@Valid @RequestBody GroupMessageDTO vo) {
        return ResultUtils.success(groupMessageService.sendMessage(vo));
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回群聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        groupMessageService.recallMessage(id);
        return ResultUtils.success();
    }

    @GetMapping("/pullOfflineMessage")
    @ApiOperation(value = "拉取离线消息", notes = "拉取离线消息,消息将通过webSocket异步推送")
    public Result pullOfflineMessage(@RequestParam Long minId) {
        groupMessageService.pullOfflineMessage(minId);
        return ResultUtils.success();
    }

    @PutMapping("/readed")
    @ApiOperation(value = "消息已读",notes="将群聊中的消息状态置为已读")
    public Result readedMessage(@RequestParam Long groupId){
        groupMessageService.readedMessage(groupId);
        return ResultUtils.success();
    }

    @GetMapping("/findReadedUsers")
    @ApiOperation(value = "获取已读用户id", notes = "获取消息已读用户列表")
    public Result<List<Long>> findReadedUsers(@RequestParam Long groupId,@RequestParam Long messageId) {
        return ResultUtils.success(groupMessageService.findReadedUsers(groupId,messageId));
    }
    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<GroupMessageVO>> recallMessage(@NotNull(message = "群聊id不能为空") @RequestParam Long groupId,
                                                      @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                      @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(groupMessageService.findHistoryMessage(groupId, page, size));
    }
}

