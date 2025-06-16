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
import xyz.qy.implatform.annotation.RepeatSubmit;
import xyz.qy.implatform.dto.GroupBanDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.vo.CommonGroupVO;
import xyz.qy.implatform.vo.GroupInviteVO;
import xyz.qy.implatform.vo.GroupJoinVO;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.GroupVO;
import xyz.qy.implatform.vo.MultCharacterGroupVO;
import xyz.qy.implatform.vo.SwitchTemplateGroupVO;
import xyz.qy.implatform.vo.TemplateGroupCreateVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊")
@RestController
@RequestMapping("/group")
public class GroupController {
    @Resource
    private IGroupService groupService;

    @RepeatSubmit
    @ApiOperation(value = "创建群聊", notes = "创建群聊")
    @PostMapping("/create")
    public Result<GroupVO> createGroup(@Valid  @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.createGroup(vo));
    }

    @ApiOperation(value = "修改群聊信息", notes = "修改群聊信息")
    @PutMapping("/modify")
    public Result<GroupVO> modifyGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.modifyGroup(vo));
    }

    @ApiOperation(value = "解散群聊", notes = "解散群聊")
    @DeleteMapping("/delete/{groupId}")
    public Result deleteGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊", notes = "查询单个群聊信息")
    @GetMapping("/find/{groupId}")
    public Result<GroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findById(groupId));
    }

    @ApiOperation(value = "查询群聊列表", notes = "查询群聊列表")
    @GetMapping("/list")
    public Result<List<GroupVO>> findGroups() {
        return ResultUtils.success(groupService.findGroups());
    }

    @ApiOperation(value = "邀请进群", notes = "邀请好友进群")
    @PostMapping("/invite")
    public Result invite(@Valid @RequestBody GroupInviteVO vo) {
        groupService.invite(vo);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询群聊成员", notes = "查询群聊成员")
    @GetMapping("/members/{groupId}")
    public Result<List<GroupMemberVO>> findGroupMembers(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findGroupMembers(groupId));
    }

    @ApiOperation(value = "退出群聊", notes = "退出群聊")
    @DeleteMapping("/quit/{groupId}")
    public Result quitGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.quitGroup(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "踢出群聊", notes = "将用户踢出群聊")
    @DeleteMapping("/kick/{groupId}")
    public Result kickGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                            @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.kickGroup(groupId, userId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "创建模板群聊群组", notes = "创建模板群聊群组")
    @PostMapping("/createTemplateGroup")
    public Result createTemplateGroup(@Valid @RequestBody TemplateGroupCreateVO vo) {
        return ResultUtils.success(groupService.createTemplateGroup(vo));
    }

    @ApiOperation(value = "切换模板群聊", notes = "切换模板群聊")
    @PostMapping("/switchTemplateGroup")
    public Result switchTemplateGroup(@Valid @RequestBody SwitchTemplateGroupVO vo) {
        return ResultUtils.success(groupService.switchTemplateGroup(vo));
    }

    @ApiOperation(value = "切换多元角色群聊", notes = "切换多元角色群聊")
    @PostMapping("/switchMultCharacterGroup")
    public Result switchMultCharacterGroup(@Valid @RequestBody MultCharacterGroupVO vo) {
        return ResultUtils.success(groupService.switchMultCharacterGroup(vo));
    }

    @ApiOperation(value = "切换角色群聊", notes = "切换角色群聊")
    @PostMapping("/switchMoreCharactersGroup")
    public Result switchMoreCharactersGroup(@Valid @RequestBody MultCharacterGroupVO vo) {
        return ResultUtils.success(groupService.switchMoreCharactersGroup(vo));
    }

    @ApiOperation(value = "切换模板角色群聊", notes = "切换模板角色群聊")
    @PostMapping("/switchTemplateMultCharactersGroup")
    public Result switchTemplateMultCharactersGroup(@Valid @RequestBody SwitchTemplateGroupVO vo) {
        return ResultUtils.success(groupService.switchTemplateMultCharactersGroup(vo));
    }

    @ApiOperation(value = "切换普通群聊", notes = "切换普通群聊")
    @PostMapping("/switchCommonGroup")
    public Result switchCommonGroup(@Valid @RequestBody CommonGroupVO vo) {
        return ResultUtils.success(groupService.switchCommonGroup(vo));
    }

    @ApiOperation(value = "查询用户未加入的群聊", notes = "查询用户未加入的群聊")
    @GetMapping("/queryNotJoinGroups")
    public Result queryNotJoinGroups(String keyWord) {
        return ResultUtils.success(groupService.queryNotJoinGroups(keyWord));
    }

    @ApiOperation(value = "申请加入群聊", notes = "申请加入群聊")
    @PostMapping("/joinGroup")
    public Result joinGroup(@Valid @RequestBody GroupJoinVO vo) {
        return ResultUtils.success(groupService.joinGroup(vo));
    }

    @ApiOperation(value = "群聊禁言", notes = "群聊禁言")
    @PostMapping("/banMsg")
    public Result banMsg(@Valid @RequestBody GroupBanDTO dto) {
        groupService.banMsg(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "解除群聊禁言", notes = "解除群聊禁言")
    @PostMapping("/unBanMsg")
    public Result unBanMsg(@Valid @RequestBody GroupBanDTO dto) {
        groupService.unBanMsg(dto);
        return ResultUtils.success();
    }
}

