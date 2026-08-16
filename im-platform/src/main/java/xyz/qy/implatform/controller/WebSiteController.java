package xyz.qy.implatform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IDictDataService;
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

    @Resource
    private IDictDataService dictDataService;

    @ApiOperation(value = "上传访客信息",notes="上传访客信息")
    @PostMapping("/report")
    public Result report() {
        visitorService.report();
        return ResultUtils.success();
    }

    @ApiOperation(value = "获取网站信息",notes="获取网站信息")
    @GetMapping("/getIcpInfo")
    public Result getIcpInfo() {
        return ResultUtils.success(dictDataService.getIcpInfo());
    }
}
