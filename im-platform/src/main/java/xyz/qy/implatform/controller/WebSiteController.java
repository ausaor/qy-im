package xyz.qy.implatform.controller;

import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IVisitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "网站信息")
@RestController
@RequestMapping("/website")
public class WebSiteController {

    @Resource
    private IVisitorService visitorService;

    @ApiOperation(value = "上传访客信息",notes="上传访客信息")
    @PostMapping("/report")
    public Result report() {
        visitorService.report();
        return ResultUtils.success();
    }
}
