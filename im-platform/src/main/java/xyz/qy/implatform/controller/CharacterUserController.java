package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.CharacterUserBindDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterUserService;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RequestMapping("/characterUser")
@RestController
public class CharacterUserController {
    @Resource
    private ICharacterUserService characterUserService;

    @ApiOperation("获取角色用户关系")
    @GetMapping("/getCharacterUsers")
    public Result getCharacterUsersByCharacterId(@RequestParam @NotNull(message = "角色id不能为空") Long characterId) {
        return ResultUtils.success(characterUserService.getCharacterUsersByCharacterId(characterId));
    }

    @ApiOperation("获取我的角色")
    @GetMapping("/getMyCharacters")
    public Result getMyCharacters() {
        return ResultUtils.success(characterUserService.getMyCharacters());
    }

    @ApiOperation("绑定角色用户关系")
    @RequestMapping("/bindCharacterUser")
    public Result bindCharacterUser(@RequestBody @Valid CharacterUserBindDTO dto) {
        characterUserService.bindCharacterUser(dto);
        return ResultUtils.success();
    }

    @ApiOperation("解绑角色用户关系")
    @RequestMapping("/unbindCharacterUser")
    public Result unbindCharacterUser(@RequestBody @Valid CharacterUserBindDTO dto) {
        characterUserService.unbindCharacterUser(dto);
        return ResultUtils.success();
    }
}
