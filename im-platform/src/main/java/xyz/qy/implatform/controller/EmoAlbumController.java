package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.dto.EmoAlbumDTO;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IEmoAlbumService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/emoAlbum")
public class EmoAlbumController {
    @Resource
    private IEmoAlbumService emoAlbumService;

    @ApiOperation("添加表情相册")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @PostMapping("/addEmoAlbum")
    public Result addEmoAlbum(@RequestBody EmoAlbumDTO dto) {
        emoAlbumService.addEmoAlbum(dto);
        return ResultUtils.success();
    }

    @ApiOperation("更新表情相册")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @PostMapping("/updateEmoAlbum")
    public Result updateEmoAlbum(@RequestBody EmoAlbumDTO dto) {
        emoAlbumService.updateEmoAlbum(dto);
        return ResultUtils.success();
    }

    @ApiOperation("删除表情相册")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @DeleteMapping("/deleteEmoAlbum")
    public Result deleteEmoAlbum(Long id) {
        emoAlbumService.deleteEmoAlbum(id);
        return ResultUtils.success();
    }
}
