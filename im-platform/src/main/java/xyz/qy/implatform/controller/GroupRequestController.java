package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import xyz.qy.implatform.dto.GroupRequestUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IGroupRequestService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 群组请求表 前端控制器
 *
 * @author Polaris
 * @since 2025-09-11
 */
@RestController
@RequestMapping("/group/request")
public class GroupRequestController {
    @Resource
    private IGroupRequestService groupRequestService;

    @ApiOperation(value = "撤回加入群聊请求", notes = "撤回加入群聊请求")
    @PostMapping("/recall")
    public Result recall(@RequestParam Long id) {
        groupRequestService.recall(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "拒绝加入群聊请求", notes = "拒绝加入群聊请求")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long id) {
        groupRequestService.reject(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "同意加入群聊请求", notes = "同意加入群聊请求")
    @PostMapping("/approve")
    public Result approve(@RequestParam Long id) {
        groupRequestService.approve(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "更新群组请求信息", notes = "更新群组请求信息")
    @PostMapping("/update")
    public Result update(@Valid @RequestBody GroupRequestUpdateDTO dto) {
        groupRequestService.update(dto);
        return ResultUtils.success();
    }
}
