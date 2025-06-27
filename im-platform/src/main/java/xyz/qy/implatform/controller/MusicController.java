package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.MusicAddDTO;
import xyz.qy.implatform.dto.MusicDelDTO;
import xyz.qy.implatform.dto.MusicQueryDTO;
import xyz.qy.implatform.dto.MusicStarDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.service.IMusicStarService;
import xyz.qy.implatform.vo.MusicVO;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class MusicController {

    private final IMusicService musicService;

    private final IMusicStarService musicStarService;

    @ApiModelProperty(value = "获取音乐列表")
    @PostMapping("/list")
    public Result<List<MusicVO>> listMusic(@Valid @RequestBody MusicQueryDTO dto) {
        return ResultUtils.success(musicService.listMusic(dto));
    }

    @ApiOperation(value = "新增音乐", notes = "新增音乐")
    @PostMapping("/add")
    public Result<MusicVO> addMusic(@RequestBody @Valid MusicAddDTO dto) {
        return ResultUtils.success(musicService.addMusic(dto));
    }

    @ApiOperation(value = "删除音乐", notes = "删除音乐")
    @DeleteMapping("/delete")
    public Result delMusic(@RequestBody @Valid MusicDelDTO dto) {
        musicService.deleteMusic(dto);
        return ResultUtils.success();
    }

    @ApiModelProperty(value = "点赞音乐")
    @PostMapping("/like")
    public Result like(@RequestBody @Valid MusicStarDTO dto) {
        musicStarService.like(dto);
        return ResultUtils.success();
    }

    @ApiModelProperty(value = "取消点赞音乐")
    @PostMapping("/cancelLike")
    public Result cancelLike(@RequestBody @Valid MusicStarDTO dto) {
        musicStarService.cancelLike(dto);
        return ResultUtils.success();
    }
}
