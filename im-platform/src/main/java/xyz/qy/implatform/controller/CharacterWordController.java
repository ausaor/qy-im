package xyz.qy.implatform.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterWordService;

import javax.annotation.Resource;

@Api(tags = "模板角色台词")
@RequestMapping("/character/word")
@RestController
public class CharacterWordController {
    @Resource
    private ICharacterWordService characterWordService;

    @ApiOperation(value = "查询已发布状态的角色台词", notes = "查询已发布状态的角色台词")
    @GetMapping("/publishedWord")
    public Result<JSONObject> findPublishedWordByCharacterId(@RequestParam("characterId") Long characterId) {
        return ResultUtils.success(characterWordService.findPublishedWordByCharacterId(characterId));
    }
}
