package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IEmoImgService;

@RestController
@RequestMapping("/emoImg")
public class EmoImgController {
    private IEmoImgService emoImgService;

    @ApiOperation("获取表情包图片列表")
    @GetMapping("/getEmoAlbumImgList")
    public Result getEmoAlbumImgList() {
        return ResultUtils.success(emoImgService.getEmoAlbumImgList());
    }
}