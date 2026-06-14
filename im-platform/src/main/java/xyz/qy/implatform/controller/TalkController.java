package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.imclient.annotation.CountLimit;
import xyz.qy.implatform.annotation.FeatureControl;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.dto.TalkAddDTO;
import xyz.qy.implatform.dto.TalkCommentDTO;
import xyz.qy.implatform.dto.TalkDelDTO;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.dto.TalkReviewDTO;
import xyz.qy.implatform.dto.TalkStarDTO;
import xyz.qy.implatform.dto.TalkUpdateDTO;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.vo.TalkVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @description: 动态
 * @author: Polaris
 * @create: 2023-11-19 22:00
 **/
@Api(tags = "动态")
@RestController
@RequestMapping("/talk")
public class TalkController {
    @Resource
    private ITalkService talkService;

    @Resource
    private ITalkStarService talkStarService;

    @Resource
    private ITalkCommentService talkCommentService;

    @ApiOperation(value = "新增动态", notes = "新增动态")
    @FeatureControl(value = "FEATURE_DYNAMIC_PUBLISH")
    @CountLimit(limitType = "talk:", count = 5, time = 24, description = "发布动态")
    @PostMapping("/add")
    public Result add(@Valid @RequestBody TalkAddDTO talkAddDTO) {
        talkService.addTalk(talkAddDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "更新动态", notes = "更新动态")
    @PostMapping("/update")
    public Result update(@Valid @RequestBody TalkUpdateDTO talkUpdateDTO) {
        talkService.updateTalk(talkUpdateDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询动态", notes = "分页查询动态")
    @PostMapping("/pageQueryTalkList")
    public Result pageQueryTalkList(@RequestBody TalkQueryDTO queryDTO) {
        return ResultUtils.success(talkService.pageQueryTalkList(queryDTO));
    }

    @ApiOperation(value = "删除动态", notes = "删除动态")
    @DeleteMapping("/delete")
    public Result del(@Valid @RequestBody TalkDelDTO talkDelDTO) {
        talkService.delTalk(talkDelDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询动态详情", notes = "查询动态详情")
    @GetMapping("/getTalkDetail/{talkId}")
    public Result<TalkVO> getTalkDetail(@NotNull(message = "参数异常") @PathVariable Long talkId) {
        return ResultUtils.success(talkService.getTalkDetail(talkId));
    }

    @RequireRoles(value = {RoleEnum.ADMIN, RoleEnum.SUPER_ADMIN})
    @ApiOperation(value = "查询动态详情", notes = "查询动态详情")
    @GetMapping("/getTalkDetailById/{talkId}")
    public Result<TalkVO> getTalkDetailById(@NotNull(message = "参数异常") @PathVariable Long talkId) {
        return ResultUtils.success(talkService.getTalkDetailById(talkId));
    }

    @FeatureControl(value = "FEATURE_DYNAMIC_LIKE")
    @ApiOperation(value = "动态点赞", notes = "动态点赞")
    @PostMapping("/like")
    public Result like(@Valid @RequestBody TalkStarDTO talkStarDTO) {
        return ResultUtils.success(talkStarService.like(talkStarDTO));
    }

    @ApiOperation(value = "取消点赞", notes = "取消点赞")
    @PostMapping("/cancelLike")
    public Result cancelLike(@Valid @RequestBody TalkStarDTO talkStarDTO) {
        talkStarService.cancelLike(talkStarDTO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "新增动态评论", notes = "新增动态评论")
    @FeatureControl(value = "FEATURE_DYNAMIC_COMMENT")
    @CountLimit(limitType = "talk-comment:", count = 50, time = 24, description = "发布动态评论")
    @PostMapping("/addTalkComment")
    public Result addTalkComment(@Valid @RequestBody TalkCommentDTO talkCommentDTO) {
        return ResultUtils.success(talkCommentService.addTalkComment(talkCommentDTO));
    }

    @ApiOperation(value = "拉取未读的动态信息", notes = "拉取未读的动态信息")
    @GetMapping("/pullOfflineTalks")
    public Result pullOfflineTalks(@RequestParam Long minId) {
        return ResultUtils.success(talkService.pullOfflineTalks(minId));
    }

    @ApiOperation(value = "分页查询动态列表", notes = "分页查询动态列表")
    @PostMapping("/pageTalkList")
    public Result pageTalkList(@RequestBody @Valid TalkQueryDTO queryDTO) {
        return ResultUtils.success(talkService.pageTalkList(queryDTO));
    }

    @RequireRoles(value = {RoleEnum.ADMIN, RoleEnum.SUPER_ADMIN})
    @ApiOperation(value = "动态审核", notes = "动态审核")
    @PostMapping("/review")
    public Result reviewTalk(@RequestBody @Valid TalkReviewDTO talkReviewDTO) {
        talkService.reviewTalk(talkReviewDTO);
        return ResultUtils.success();
    }
}
