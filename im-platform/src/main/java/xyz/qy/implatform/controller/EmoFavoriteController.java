package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.EmoFavoriteDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IEmoFavoriteService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/emoFavorite")
public class EmoFavoriteController {
    @Resource
    private IEmoFavoriteService emoFavoriteService;

    @ApiOperation("添加表情包收藏")
    @PostMapping("/addEmoFavorite")
    public Result addEmoFavorite(@RequestBody EmoFavoriteDTO dto) {
        emoFavoriteService.addEmoFavorite(dto);
        return ResultUtils.success();
    }

    @ApiOperation("删除表情包收藏")
    @DeleteMapping("/deleteEmoFavorite")
    public Result deleteEmoFavorite(Long id) {
        emoFavoriteService.deleteEmoFavorite(id);
        return ResultUtils.success();
    }
}