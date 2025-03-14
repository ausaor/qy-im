package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.impl.EmailService;

import javax.annotation.Resource;

@Api(tags = "邮件相关接口")
@RestController
@RequestMapping("/email")
public class EmailController {
    @Resource
    private EmailService emailService;

    @GetMapping(value = "/getCode")
    public Result<String> getEmailCode(String toEmail) {
        emailService.getEmailCode(toEmail);
        return ResultUtils.success();
    }
}
