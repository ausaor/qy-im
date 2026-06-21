package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.ShortVideoCommentAddDTO;
import xyz.qy.implatform.dto.ShortVideoCommentDelDTO;
import xyz.qy.implatform.dto.ShortVideoCommentQueryDTO;
import xyz.qy.implatform.dto.ShortVideoCommentUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoCommentService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoCommentVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "短视频评论")
@Validated
@RestController
@RequestMapping("/shortVideoComment")
public class ShortVideoCommentController {
    @Resource
    private IShortVideoCommentService shortVideoCommentService;

    @ApiOperation(value = "新增评论", notes = "新增评论")
    @PostMapping("/add")
    public Result<ShortVideoCommentVO> add(@Valid @RequestBody ShortVideoCommentAddDTO dto) {
        return ResultUtils.success(shortVideoCommentService.addShortVideoComment(dto));
    }

    @ApiOperation(value = "删除评论", notes = "删除评论")
    @DeleteMapping("/delete")
    public Result delete(@Valid @RequestBody ShortVideoCommentDelDTO dto) {
        shortVideoCommentService.deleteShortVideoComment(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "评论列表", notes = "评论列表")
    @PostMapping("/list")
    public Result<List<ShortVideoCommentVO>> list(@RequestBody ShortVideoCommentQueryDTO dto) {
        return ResultUtils.success(shortVideoCommentService.listShortVideoComments(dto));
    }

    @ApiOperation(value = "评论分页列表", notes = "评论分页列表")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoCommentVO>>> pageList(@RequestBody ShortVideoCommentQueryDTO dto) {
        return ResultUtils.success(shortVideoCommentService.pageShortVideoComments(dto));
    }
}
