package xyz.qy.implatform.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateGroupVO;
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

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 14:18
 **/
@Api(tags = "模板群聊")
@RestController
@RequestMapping("/templateGroup")
public class TemplateGroupController {

    @Resource
    private ITemplateGroupService templateGroupService;

    @PostMapping("/addOrModify")
    public Result addOrModify(@RequestBody TemplateGroupVO vo) {
        templateGroupService.addOrModify(vo);
        return ResultUtils.success();
    }

    @ApiOperation(value = "删除模板群聊",notes="删除模板群聊")
    @DeleteMapping("/delete/{id}")
    public Result deleteTemplateGroup(@NotNull(message = "模板群聊id不能为空") @PathVariable Long id){
        templateGroupService.delete(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询模板群聊",notes="查询模板群聊")
    @GetMapping("/list")
    public Result<List<TemplateGroupVO>> listTemplateGroups(TemplateGroupVO templateGroupVO) {
        return ResultUtils.success(templateGroupService.findTemplateGroups(templateGroupVO));
    }

    @ApiOperation(value = "查询我的模板群聊",notes="查询我的模板群聊")
    @GetMapping("/findMyTemplateGroups")
    public Result<List<TemplateGroupVO>> findMyTemplateGroups() {
        return ResultUtils.success(templateGroupService.findMyTemplateGroups());
    }

    @ApiOperation(value = "分页查询所有模板群聊",notes="分页查询所有模板群聊")
    @GetMapping("/findAllTemplateGroups")
    public Result<List<TemplateGroupVO>> findAllTemplateGroups() {
        return ResultUtils.success(templateGroupService.pageAllTemplateGroups());
    }

    @ApiOperation(value = "模板群聊提交审批",notes="模板群聊提交审批")
    @PostMapping("/submitForApproval")
    public Result submitForApproval(@RequestBody JSONObject object) {
        Long id = object.getLong("id");
        if (ObjectUtil.isNull(id)) {
            throw new GlobalException("参数异常");
        }
        templateGroupService.submitForApproval(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "模板群聊撤回审批",notes="模板群聊撤回审批")
    @PostMapping("/withdrawalOfApproval")
    public Result withdrawalOfApproval(@RequestBody JSONObject object) {
        Long id = object.getLong("id");
        if (ObjectUtil.isNull(id)) {
            throw new GlobalException("参数异常");
        }
        templateGroupService.withdrawalOfApproval(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "查询审批中的模板群聊", notes = "查询审批中的模板群聊")
    @GetMapping("/findReviewingTemplateGroups")
    public Result<List<TemplateGroupVO>> findReviewingTemplateGroups() {
        return ResultUtils.success(templateGroupService.findReviewingTemplateGroups());
    }

    @ApiOperation(value = "提交审核结论", notes = "提交审核结论")
    @PostMapping("/submitAuditConclusion")
    public Result submitAuditConclusion(@RequestBody ReviewVO reviewVO) {
        templateGroupService.submitAuditConclusion(reviewVO);
        return ResultUtils.success();
    }
}
