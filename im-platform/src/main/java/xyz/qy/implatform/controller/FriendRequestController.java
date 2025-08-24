package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IFriendRequestService;
import xyz.qy.implatform.vo.FriendRequestVO;

import javax.annotation.Resource;
import java.util.List;

/**
 * 请求添加好友表 前端控制器
 *
 * @author Polaris
 * @since 2025-08-24
 */
@Api(tags = "请求添加好友表")
@RestController
@RequestMapping("/friend/request")
public class FriendRequestController {
    @Resource
    private IFriendRequestService friendRequestService;

    @GetMapping("/list")
    @ApiOperation(value = "好友请求列表", notes = "获取好友请求列表")
    public Result<List<FriendRequestVO>> friendRequestList() {
        return ResultUtils.success(friendRequestService.friendRequestList());
    }

    @ApiOperation(value = "同意好友请求", notes = "同意好友请求")
    @PostMapping("/approve")
    public Result approve(@RequestParam Long id) {
        friendRequestService.approve(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "拒绝好友请求", notes = "拒绝好友请求")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long id) {
        friendRequestService.reject(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "撤回好友请求", notes = "撤回好友请求")
    @PostMapping("/recall")
    public Result recall(@RequestParam Long id) {
        friendRequestService.recall(id);
        return ResultUtils.success();
    }
}
