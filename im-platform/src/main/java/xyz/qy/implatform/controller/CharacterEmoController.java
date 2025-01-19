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
import xyz.qy.implatform.service.ICharacterEmoService;

import javax.annotation.Resource;

@Api(tags = "模板角色表情包")
@RequestMapping("/character/emo")
@RestController
public class CharacterEmoController {
    @Resource
    private ICharacterEmoService characterEmoService;

    @ApiOperation(value = "查询已发布状态的角色表情包", notes = "查询已发布状态的角色表情包")
    @GetMapping("/publishedEmo")
    public Result<JSONObject> findPublishedEmoByCharacterId(@RequestParam("characterId") Long characterId) {
        return ResultUtils.success(characterEmoService.findPublishedEmoByCharacterId(characterId));
    }
}
