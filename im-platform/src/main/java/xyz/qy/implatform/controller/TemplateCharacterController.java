package xyz.qy.implatform.controller;

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
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.vo.SelectableTemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description: 模板特征人物控制层
 * @author: Polaris
 * @create: 2023-03-12 16:47
 **/
@Api(tags = "模板特征人物")
@RestController
@RequestMapping("/templateCharacter")
public class TemplateCharacterController {

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @ApiOperation(value = "查询模板角色", notes = "查询模板角色")
    @GetMapping("/list/{templateGroupId}")
    public Result<List<TemplateCharacterVO>> findTemplateCharactersByGroupId(@NotNull(message = "模板群聊id不能为空") @PathVariable Long templateGroupId) {
        return ResultUtils.success(templateCharacterService.findTemplateCharactersByGroupId(templateGroupId));
    }

    @ApiOperation(value = "查询已发布模板角色id", notes = "查询已发布模板角色id")
    @GetMapping("/characterIds")
    public Result<List<Long>> findPublishedCharacterIdsByGroupId(@NotNull(message = "群聊模板id不能为空") Long templateGroupId) {
        return ResultUtils.success(templateCharacterService.findPublishedCharacterIdsByGroupId(templateGroupId));
    }

    @ApiOperation(value = "查询已发布模板角色", notes = "查询已发布模板角色")
    @GetMapping("/list-published/{templateGroupId}")
    public Result<List<TemplateCharacterVO>> findPublishedByGroupId(@NotNull(message = "模板群聊id不能为空") @PathVariable Long templateGroupId) {
        return ResultUtils.success(templateCharacterService.findPublishedByGroupId(templateGroupId));
    }

    @ApiOperation(value = "查询模板群聊可选择的模板人物", notes = "查询模板群聊可选择的模板人物")
    @PostMapping("/findSelectableTemplateCharacter")
    public Result<List<TemplateCharacterVO>> findSelectableTemplateCharacter(@RequestBody SelectableTemplateCharacterVO vo) {
        return ResultUtils.success(templateCharacterService.findSelectableTemplateCharacter(vo));
    }

    @ApiOperation(value = "通过id查询模板角色信息", notes = "通过id查询模板角色信息")
    @GetMapping("/findByCharacterId")
    public Result<TemplateCharacterVO> findByCharacterId(@RequestParam @NotNull(message = "id不能为空") Long id) {
        return ResultUtils.success(templateCharacterService.findByCharacterId(id));
    }

    @ApiOperation(value = "新增或修改模板人物信息", notes = "新增或修改模板人物信息")
    @PostMapping("/addOrModifyTemplateCharacters")
    public Result addOrModifyTemplateCharacters(@RequestBody TemplateGroupVO templateGroupVO) {
        templateCharacterService.addOrModifyTemplateCharacters(templateGroupVO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "删除模板人物", notes = "删除模板人物")
    @DeleteMapping("/delete/{id}")
    public Result deleteTemplateGroup(@NotNull(message = "模板人物id不能为空") @PathVariable Long id) {
        templateCharacterService.delete(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "获取指定数量的模板角色", notes = "获取指定数量的模板角色")
    @GetMapping("/getRandomCharacters")
    public Result<List<TemplateCharacterVO>> getRandomCharacters(@NotNull(message = "数量不能为空") Integer count) {
        return ResultUtils.success(templateCharacterService.getRandomCharacters(count));
    }
}
