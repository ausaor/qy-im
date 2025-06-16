package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITalkCommentService;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * @description: 动态评论
 * @author: Polaris
 * @create: 2024-01-07 08:31
 **/
@Api(tags = "动态评论")
@RestController
@RequestMapping("/talkComment")
public class TalkCommentController {
    @Resource
    private ITalkCommentService talkCommentService;

    @ApiModelProperty(value = "删除动态", notes = "删除动态")
    @DeleteMapping("/delete/{commentId}")
    public Result deleteTalkComment(@NotNull(message = "入参异常") @PathVariable Long commentId) {
        talkCommentService.deleteTalkComment(commentId);
        return ResultUtils.success();
    }
}
