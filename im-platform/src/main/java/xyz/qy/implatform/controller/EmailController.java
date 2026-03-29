package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.EmailDTO;
import xyz.qy.implatform.dto.ResetPwdDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.service.impl.EmailService;

import javax.annotation.Resource;
import javax.validation.Valid;

@Api(tags = "邮件相关接口")
@RestController
@RequestMapping("/email")
public class EmailController {
    @Resource
    private EmailService emailService;

    @Resource
    private IUserService userService;

    @ApiOperation(value = "获取邮件验证码", notes = "获取邮件验证码")
    @PostMapping(value = "/getCode")
    public Result<String> getEmailCode(@Valid @RequestBody EmailDTO dto) {
        emailService.getEmailCode(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "重置密码", notes = "重置密码")
    @PostMapping(value = "/resetPwd")
    public Result<String> resetPwd(@Valid @RequestBody ResetPwdDTO dto) {
        userService.resetPwdByEmail(dto);
        return ResultUtils.success();
    }
}
