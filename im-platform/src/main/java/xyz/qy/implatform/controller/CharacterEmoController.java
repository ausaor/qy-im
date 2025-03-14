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
import xyz.qy.implatform.dto.CharacterEmoDTO;
import xyz.qy.implatform.dto.CharacterEmoSaveDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterEmoService;
import xyz.qy.implatform.vo.CharacterEmoVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    @ApiOperation(value = "查询角色表情", notes = "查询角色表情")
    @PostMapping("/findCharacterEmos")
    public Result<List<CharacterEmoVO>> findCharacterEmos(@Valid @RequestBody CharacterEmoDTO dto) {
        return ResultUtils.success(characterEmoService.findCharacterEmos(dto));
    }

    @ApiOperation(value = "删除角色表情", notes = "删除角色表情")
    @DeleteMapping("/delete/{id}")
    public Result delete(@NotNull(message = "主键为空") @PathVariable Long id) {
        characterEmoService.delete(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "保存角色表情", notes = "保存角色表情")
    @PostMapping("/save")
    public Result save(@RequestBody CharacterEmoSaveDTO dto) {
        characterEmoService.save(dto);
        return ResultUtils.success();
    }
}
