package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import xyz.qy.imclient.annotation.CountLimit;
import xyz.qy.implatform.dto.ComplaintRecordDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IComplaintRecordService;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 投诉记录控制器
 *
 * @author Lingma
 * @since 2026-05-05
 */
@Api(tags = "投诉记录")
@RestController
@RequestMapping("/complaint")
public class ComplaintRecordController {

    @Resource
    private IComplaintRecordService complaintRecordService;

    @CountLimit(limitType = "complaint:", count = 10, time = 24, description = "提交投诉")
    @ApiOperation(value = "提交投诉", notes = "用户提交投诉信息")
    @PostMapping("/submit")
    public Result submitComplaint(@RequestBody @Valid ComplaintRecordDTO dto) {
        complaintRecordService.submitComplaint(dto);
        return ResultUtils.success();
    }
}