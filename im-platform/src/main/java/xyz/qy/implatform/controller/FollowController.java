package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.RepeatSubmit;
import xyz.qy.implatform.dto.FollowDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IFollowService;
import xyz.qy.implatform.vo.FollowVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Api(tags = "关注")
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @RepeatSubmit
    @PostMapping("/add")
    @ApiOperation(value = "添加关注", notes = "关注目标")
    public Result addFollow(@RequestBody @Valid FollowDTO dto) {
        followService.addFollow(dto);
        return ResultUtils.success();
    }

    @DeleteMapping("/cancel")
    @ApiOperation(value = "取消关注", notes = "取消关注目标")
    public Result cancelFollow(@RequestParam("targetId") Long targetId,
                               @RequestParam("type") String type) {
        followService.cancelFollow(targetId, type);
        return ResultUtils.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "关注列表", notes = "获取当前用户的所有关注")
    public Result<List<FollowVO>> findFollows(String type) {
        return ResultUtils.success(followService.findFollows(type));
    }
}
