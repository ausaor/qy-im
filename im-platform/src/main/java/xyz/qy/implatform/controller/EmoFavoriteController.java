package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.EmoFavoriteDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IEmoFavoriteService;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/emoFavorite")
public class EmoFavoriteController {
    @Resource
    private IEmoFavoriteService emoFavoriteService;



    @ApiOperation("添加表情包收藏")
    @PostMapping("/addEmoFavorite")
    public Result addEmoFavorite(@Valid @RequestBody EmoFavoriteDTO dto) {
        return ResultUtils.success(emoFavoriteService.addEmoFavorite(dto));
    }

    @ApiOperation("删除表情包收藏")
    @PostMapping("/delete")
    public Result deleteEmoFavorite(@RequestBody EmoFavoriteDTO dto) {
        emoFavoriteService.deleteEmoFavorite(dto.getId());
        return ResultUtils.success();
    }

    @ApiOperation("获取表情包收藏列表")
    @GetMapping("/list")
    public Result list() {
        return ResultUtils.success(emoFavoriteService.listEmoFavorite());
    }
}