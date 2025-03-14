package xyz.qy.implatform.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.vo.CharacterAvatarVO;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;

import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "模板人物头像")
@RestController
@RequestMapping("/characterAvatar")
public class CharacterAvatarController {
    @Autowired
    private ICharacterAvatarService characterAvatarService;

    @ApiOperation(value = "根据模板人物id查询已发布模板人物头像", notes = "根据模板人物id查询已发布模板人物头像")
    @GetMapping("/list/{templateCharacterId}")
    public Result<List<CharacterAvatarVO>> queryPublishedCharacterAvatarByCharacterId(@NotNull(message = "模板人物id不能为空") @PathVariable Long templateCharacterId) {
        return ResultUtils.success(characterAvatarService.queryPublishedCharacterAvatarByCharacterId(templateCharacterId));
    }

    @ApiOperation(value = "根据模板人物id查询所有模板人物头像", notes = "根据模板人物id查询所有模板人物头像")
    @GetMapping("/listAll/{templateCharacterId}")
    public Result<List<CharacterAvatarVO>> queryAllCharacterAvatarByCharacterId(@NotNull(message = "模板人物id不能为空") @PathVariable Long templateCharacterId) {
        return ResultUtils.success(characterAvatarService.queryAllCharacterAvatarByCharacterId(templateCharacterId));
    }

    @ApiOperation(value = "删除模板人物头像", notes = "删除模板人物头像")
    @DeleteMapping("/delete/{id}")
    public Result delete(@NotNull(message = "模板人物id不能为空") @PathVariable Long id) {
        characterAvatarService.delete(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "新增或修改模板人物头像", notes = "新增或修改模板人物头像")
    @PostMapping("/addOrModify")
    public Result addOrModifyCharacterAvatars(@RequestBody TemplateCharacterVO templateCharacterVO) {
        characterAvatarService.addOrModifyCharacterAvatars(templateCharacterVO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "模板人物头像提交审核", notes = "模板人物头像提交审核")
    @PostMapping("/submitForApproval")
    public Result submitForApproval(@RequestBody JSONObject jsonObject) {
        Long templateCharacterId = jsonObject.getLong("id");
        if (ObjectUtil.isNull(templateCharacterId)) {
            throw new GlobalException("参数异常");
        }
        characterAvatarService.submitForApproval(templateCharacterId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "撤回审核", notes = "撤回审核")
    @PostMapping("/withdrawalOfApproval")
    public Result withdrawalOfApproval(@NotNull(message = "模板人物id不能为空") @RequestBody JSONObject jsonObject) {
        Long templateCharacterId = jsonObject.getLong("id");
        if (ObjectUtil.isNull(templateCharacterId)) {
            throw new GlobalException("参数异常");
        }
        characterAvatarService.withdrawalOfApproval(templateCharacterId);
        return ResultUtils.success();
    }

    @ApiOperation(value = "提交审核结论", notes = "提交审核结论")
    @PostMapping("/submitAuditConclusion")
    public Result submitAuditConclusion(@RequestBody ReviewVO reviewVO) {
        characterAvatarService.submitAuditConclusion(reviewVO);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询审核中的人物头像", notes = "查询审核中的人物头像")
    @GetMapping("/findReviewingCharacterAvatar")
    public Result<List<TemplateCharacterVO>> findReviewingCharacterAvatar() {
        return ResultUtils.success(characterAvatarService.findReviewingCharacterAvatar());
    }
}
