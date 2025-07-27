package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.impl.SystemService;
import xyz.qy.implatform.vo.SystemConfigVO;

@Api(tags = "系统相关")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {
    private final SystemService systemService;

    @GetMapping("/config")
    @ApiOperation(value = "加载系统配置", notes = "加载系统配置")
    public Result<SystemConfigVO> loadConfig() {
        return ResultUtils.success(systemService.getSystemConfig());
    }

    @GetMapping("/allBanned")
    @ApiOperation(value = "全局禁言", notes = "全局禁言")
    public Result allBanned(Integer time) {
        systemService.allBanned(time);
        return ResultUtils.success();
    }

    @GetMapping("/unAllBanned")
    @ApiOperation(value = "解除全局禁言", notes = "解除全局禁言")
    public Result unAllBanned() {
        systemService.unAllBanned();
        return ResultUtils.success();
    }
}

