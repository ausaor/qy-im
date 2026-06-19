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
import xyz.qy.implatform.dto.ShortVideoAddDTO;
import xyz.qy.implatform.dto.ShortVideoDelDTO;
import xyz.qy.implatform.dto.ShortVideoQueryDTO;
import xyz.qy.implatform.dto.ShortVideoUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "短视频")
@Validated
@RestController
@RequestMapping("/shortVideo")
public class ShortVideoController {
    @Resource
    private IShortVideoService shortVideoService;

    @ApiOperation(value = "新增短视频", notes = "新增短视频")
    @PostMapping("/add")
    public Result<ShortVideoVO> add(@Valid @RequestBody ShortVideoAddDTO dto) {
        return ResultUtils.success(shortVideoService.addShortVideo(dto));
    }

    @ApiOperation(value = "更新短视频", notes = "更新短视频")
    @PostMapping("/update")
    public Result<ShortVideoVO> update(@Valid @RequestBody ShortVideoUpdateDTO dto) {
        return ResultUtils.success(shortVideoService.updateShortVideo(dto));
    }

    @ApiOperation(value = "删除短视频", notes = "删除短视频")
    @DeleteMapping("/delete")
    public Result delete(@Valid @RequestBody ShortVideoDelDTO dto) {
        shortVideoService.deleteShortVideo(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "短视频详情", notes = "短视频详情")
    @GetMapping("/detail/{id}")
    public Result<ShortVideoVO> detail(@NotNull(message = "id不能为空") @PathVariable Long id) {
        return ResultUtils.success(shortVideoService.getShortVideoById(id));
    }

    @ApiOperation(value = "我的短视频", notes = "我的短视频")
    @PostMapping("/my")
    public Result<List<ShortVideoVO>> myShortVideos(@RequestBody ShortVideoQueryDTO dto) {
        return ResultUtils.success(shortVideoService.myShortVideos(dto));
    }

    @ApiOperation(value = "我点赞的短视频", notes = "我点赞的短视频")
    @PostMapping("/myLiked")
    public Result<List<ShortVideoVO>> myLikedShortVideos() {
        return ResultUtils.success(shortVideoService.myLikedShortVideos());
    }

    @ApiOperation(value = "我收藏的短视频", notes = "我收藏的短视频")
    @PostMapping("/myFavorite")
    public Result<List<ShortVideoVO>> myFavoriteShortVideos() {
        return ResultUtils.success(shortVideoService.myFavoriteShortVideos());
    }

    @ApiOperation(value = "短视频列表", notes = "短视频列表")
    @PostMapping("/list")
    public Result<List<ShortVideoVO>> list(@RequestBody ShortVideoQueryDTO dto) {
        return ResultUtils.success(shortVideoService.listShortVideos(dto));
    }

    @ApiOperation(value = "短视频分页列表", notes = "短视频分页列表")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoVO>>> pageList(@RequestBody ShortVideoQueryDTO dto) {
        return ResultUtils.success(shortVideoService.pageShortVideos(dto));
    }
}
