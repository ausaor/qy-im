package xyz.qy.implatform.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.CharacterWordDTO;
import xyz.qy.implatform.dto.CharacterWordSaveDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterWordService;
import xyz.qy.implatform.vo.CharacterWordVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    @ApiOperation(value = "查询角色台词", notes = "查询角色台词")
    @PostMapping("/findCharacterWords")
    public Result<List<CharacterWordVO>> findCharacterWords(@Valid @RequestBody CharacterWordDTO dto) {
        return ResultUtils.success(characterWordService.findCharacterWords(dto));
    }

    @ApiOperation(value = "保存角色台词", notes = "保存角色台词")
    @PostMapping("/save")
    public Result save(@RequestBody CharacterWordSaveDTO dto) {
        characterWordService.save(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "删除角色台词", notes = "删除角色台词")
    @DeleteMapping("/delete/{id}")
    public Result delete(@NotNull(message = "主键为空") @PathVariable Long id) {
        characterWordService.delete(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "发布角色台词", notes = "发布角色台词")
    @PostMapping("/publish")
    public Result publish(@RequestBody CharacterWordDTO dto) {
        characterWordService.publish(dto);
        return ResultUtils.success();
    }
}
