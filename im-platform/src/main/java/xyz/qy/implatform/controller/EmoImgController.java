package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IEmoImgService;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/emoImg")
public class EmoImgController {
    @Resource
    private IEmoImgService emoImgService;

    @ApiOperation("获取表情包图片列表")
    @GetMapping("/getEmoAlbumImgList")
    public Result getEmoAlbumImgList() {
        return ResultUtils.success(emoImgService.getEmoAlbumImgList());
    }

    @ApiOperation("搜索表情包")
    @GetMapping("/search")
    public Result search(@NotBlank(message = "名称不能为空") String name) {
        return ResultUtils.success(emoImgService.search(name));
    }
}