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
import xyz.qy.implatform.dto.ShortVideoLikeAddDTO;
import xyz.qy.implatform.dto.ShortVideoLikeDelDTO;
import xyz.qy.implatform.dto.ShortVideoLikeQueryDTO;
import xyz.qy.implatform.dto.ShortVideoLikeUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoLikeService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoLikeVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "短视频点赞")
@Validated
@RestController
@RequestMapping("/shortVideoLike")
public class ShortVideoLikeController {
    @Resource
    private IShortVideoLikeService shortVideoLikeService;

    @ApiOperation(value = "新增点赞", notes = "新增点赞")
    @PostMapping("/add")
    public Result<ShortVideoLikeVO> add(@Valid @RequestBody ShortVideoLikeAddDTO dto) {
        return ResultUtils.success(shortVideoLikeService.addShortVideoLike(dto));
    }

    @ApiOperation(value = "更新点赞", notes = "更新点赞")
    @PostMapping("/update")
    public Result<ShortVideoLikeVO> update(@Valid @RequestBody ShortVideoLikeUpdateDTO dto) {
        return ResultUtils.success(shortVideoLikeService.updateShortVideoLike(dto));
    }

    @ApiOperation(value = "删除点赞", notes = "删除点赞")
    @DeleteMapping("/delete")
    public Result delete(@Valid @RequestBody ShortVideoLikeDelDTO dto) {
        shortVideoLikeService.deleteShortVideoLike(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "点赞详情", notes = "点赞详情")
    @GetMapping("/detail/{id}")
    public Result<ShortVideoLikeVO> detail(@NotNull(message = "id不能为空") @PathVariable Long id) {
        return ResultUtils.success(shortVideoLikeService.getShortVideoLikeById(id));
    }

    @ApiOperation(value = "点赞列表", notes = "点赞列表")
    @PostMapping("/list")
    public Result<List<ShortVideoLikeVO>> list(@RequestBody ShortVideoLikeQueryDTO dto) {
        return ResultUtils.success(shortVideoLikeService.listShortVideoLikes(dto));
    }

    @ApiOperation(value = "点赞分页列表", notes = "点赞分页列表")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoLikeVO>>> pageList(@RequestBody ShortVideoLikeQueryDTO dto) {
        return ResultUtils.success(shortVideoLikeService.pageShortVideoLikes(dto));
    }
}
