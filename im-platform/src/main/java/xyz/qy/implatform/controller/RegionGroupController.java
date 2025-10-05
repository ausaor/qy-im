package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.RegionGroupBanDTO;
import xyz.qy.implatform.dto.RegionGroupDTO;
import xyz.qy.implatform.dto.RegionGroupMemberDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.vo.RegionGroupMemberVO;
import xyz.qy.implatform.vo.RegionGroupVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 地区群聊controller
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Api(tags = "地区群聊")
@Validated
@RestController
@RequestMapping("/region/group")
public class RegionGroupController {
    @Resource
    private IRegionGroupService regionGroupService;
    @ApiOperation(value = "查询地区群聊列表", notes = "查询地区群聊列表")
    @GetMapping("/list")
    public Result<List<RegionGroupVO>> findGroups() {
        return ResultUtils.success(regionGroupService.findRegionGroups());
    }

    @ApiOperation(value = "查询地区群聊", notes = "查询单个地区群聊信息")
    @GetMapping("/find/{regionGroupId}")
    public Result<RegionGroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long regionGroupId) {
        return ResultUtils.success(regionGroupService.findById(regionGroupId));
    }

    @ApiOperation(value = "查询地区群聊", notes = "查询地区群聊")
    @GetMapping("/findRegionGroupsByCode")
    public Result<List<RegionGroupVO>> findRegionGroupsByCode(@NotBlank(message = "地区群聊code不能为空") @RequestParam String code) {
        return ResultUtils.success(regionGroupService.findRegionGroupsByCode(code));
    }

    @ApiOperation(value = "修改地区群聊成员信息", notes = "修改地区群聊成员信息")
    @PostMapping("/modifyRegionGroupMember")
    public Result<RegionGroupMemberVO> modifyRegionGroupMember(@RequestBody RegionGroupMemberDTO dto) {
        return ResultUtils.success(regionGroupService.modifyRegionGroupMember(dto));
    }

    @ApiOperation(value = "查询地区群聊成员", notes = "查询地区群聊成员")
    @GetMapping("/members/{regionGroupId}")
    public Result<List<RegionGroupMemberVO>> findRegionGroupMembers(@NotNull(message = "地区群聊id不能为空") @PathVariable Long regionGroupId) {
        return ResultUtils.success(regionGroupService.findRegionGroupMembers(regionGroupId));
    }

    @ApiOperation(value = "加入地区群聊", notes = "加入地区群聊")
    @PostMapping("/join")
    public Result<RegionGroupVO> joinRegionGroup(@Valid @RequestBody RegionGroupDTO regionGroupDTO) {
        return ResultUtils.success(regionGroupService.joinRegionGroup(regionGroupDTO));
    }

    @ApiOperation(value = "退出地区群聊", notes = "退出地区群聊")
    @PostMapping(value = "/quit")
    public Result quitRegionGroup(@Valid @RequestBody RegionGroupDTO regionGroupDTO) {
        regionGroupService.quitRegionGroup(regionGroupDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "投票选举地区群聊群主", notes = "投票选举地区群聊群主")
    @PostMapping("/vote")
    public Result vote(@RequestBody RegionGroupMemberDTO dto) {
        regionGroupService.voteRegionGroupLeader(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "投票解除地区群聊群主", notes = "投票解除地区群聊群主")
    @PostMapping("/voteRemove")
    public Result voteRemove(@RequestBody RegionGroupMemberDTO dto) {
        regionGroupService.voteRemoveRegionGroupLeader(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "地区群聊禁言", notes = "地区群聊禁言")
    @PostMapping("/banMsg")
    public Result banMsg(@Valid @RequestBody RegionGroupBanDTO dto) {
        regionGroupService.banMsg(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "解除地区群聊禁言", notes = "解除地区群聊禁言")
    @PostMapping("/unBanMsg")
    public Result unBanMsg(@Valid @RequestBody RegionGroupBanDTO dto) {
        regionGroupService.unBanMsg(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "地区群聊群主转移", notes = "地区群聊群主转移")
    @PostMapping("/leaderTransfer")
    public Result leaderTransfer(@RequestBody RegionGroupMemberDTO dto) {
        regionGroupService.leaderTransfer(dto);
        return ResultUtils.success();
    }
}
