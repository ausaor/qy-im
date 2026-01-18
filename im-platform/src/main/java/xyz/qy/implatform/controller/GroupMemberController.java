package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.SwitchCharacterAvatarVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊成员")
@RestController
@RequestMapping("/group/member")
public class GroupMemberController {

    @Resource
    private IGroupMemberService groupMemberService;

    @ApiOperation(value = "切换模板人物", notes = "切换模板人物")
    @PostMapping("/switchTemplateCharacter")
    public Result<GroupMemberVO> switchTemplateCharacter(@RequestBody GroupMemberVO groupMemberVO) {
        groupMemberService.switchTemplateCharacter(groupMemberVO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "切换模板人物头像", notes = "切换模板人物头像")
    @PostMapping("/switchCharacterAvatar")
    public Result switchCharacterAvatar(@Valid @RequestBody SwitchCharacterAvatarVO avatarVO) {
        groupMemberService.switchCharacterAvatar(avatarVO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "获取群聊角色id", notes = "获取群聊角色id")
    @GetMapping("/getGroupCharacterIds")
    public Result<List<Long>> getGroupCharacterIds(@NotNull(message = "群聊id不能为空") Long groupId) {
        return ResultUtils.success(groupMemberService.getGroupCharacterIds(groupId));
    }
}
