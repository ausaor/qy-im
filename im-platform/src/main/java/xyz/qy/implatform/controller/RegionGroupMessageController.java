package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import xyz.qy.implatform.dto.RegionGroupMessageDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IRegionGroupMessageService;
import xyz.qy.implatform.vo.RegionGroupMessageVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 地区群聊消息
 *
 * @author Polaris
 * @since 2022-11-03
 */
@Api(tags = "地区群聊消息")
@RestController
@RequestMapping("/message/regionGroup")
public class RegionGroupMessageController {

    @Resource
    private IRegionGroupMessageService regionGroupMessageService;

    @BanSendMsg(msgType = "regionGroup")
    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息", notes = "发送群聊消息")
    public Result<RegionGroupMessageVO> sendMessage(@Valid @RequestBody RegionGroupMessageDTO dto) {
        return ResultUtils.success(regionGroupMessageService.sendMessage(dto));
    }

    @GetMapping("/pullOfflineMessage")
    @ApiOperation(value = "拉取消息",notes="拉取消息,一次最多拉取100条")
    public Result<List<RegionGroupMessageVO>> loadMessage(@RequestParam Long minId) {
        regionGroupMessageService.pullOfflineMessage(minId);
        return ResultUtils.success();
    }

    @PutMapping("/readed")
    @ApiOperation(value = "消息已读",notes="将地区群聊中的消息状态置为已读")
    public Result readedMessage(@RequestParam Long regionGroupId){
        regionGroupMessageService.readedMessage(regionGroupId);
        return ResultUtils.success();
    }

    @GetMapping("/findReadedUsers")
    @ApiOperation(value = "获取已读用户id", notes = "获取消息已读用户列表")
    public Result<List<Long>> findReadedUsers(@RequestParam Long groupId,@RequestParam Long messageId) {
        return ResultUtils.success(regionGroupMessageService.findReadedUsers(groupId,messageId));
    }

    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回群聊消息")
    public Result<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        regionGroupMessageService.recallMessage(id);
        return ResultUtils.success();
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public Result<List<RegionGroupMessageVO>> recallMessage(@NotNull(message = "群聊id不能为空") @RequestParam Long regionGroupId,
                                                      @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                      @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResultUtils.success(regionGroupMessageService.findHistoryMessage(regionGroupId, page, size));
    }
}
