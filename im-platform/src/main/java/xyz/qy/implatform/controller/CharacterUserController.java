package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.CharacterUserBindDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterUserService;

import javax.annotation.Resource;
import javax.validation.Valid;

@RequestMapping("/characterUser")
@RestController
public class CharacterUserController {
    @Resource
    private ICharacterUserService characterUserService;

    @ApiOperation("绑定角色用户关系")
    @RequestMapping("/bindCharacterUser")
    public Result bindCharacterUser(@RequestBody @Valid CharacterUserBindDTO dto) {
        characterUserService.bindCharacterUser(dto);
        return ResultUtils.success();
    }
}
