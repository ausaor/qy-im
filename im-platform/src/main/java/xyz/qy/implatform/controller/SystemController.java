package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.impl.SystemService;
import xyz.qy.implatform.vo.SystemConfigVO;

@Api(tags = "系统相关")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {
    private final SystemService systemService;

    private final IMediaMaterialService mediaMaterialService;

    @GetMapping("/config")
    @ApiOperation(value = "加载系统配置", notes = "加载系统配置")
    public Result<SystemConfigVO> loadConfig() {
        return ResultUtils.success(systemService.getSystemConfig());
    }

    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @GetMapping("/allBanned")
    @ApiOperation(value = "全局禁言", notes = "全局禁言")
    public Result allBanned(Integer time) {
        systemService.allBanned(time);
        return ResultUtils.success();
    }

    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @GetMapping("/unAllBanned")
    @ApiOperation(value = "解除全局禁言", notes = "解除全局禁言")
    public Result unAllBanned() {
        systemService.unAllBanned();
        return ResultUtils.success();
    }

    @ApiOperation(value = "设置媒体播放素材", notes = "设置媒体播放素材")
    @RequestMapping("/setPlayMediaMaterial")
    public Result setPlayMediaMaterial(Integer sort) {
        return ResultUtils.success(mediaMaterialService.setLoginPagePlayMediaMaterial(sort));
    }

    @ApiOperation(value = "生成随机排序号", notes = "生成随机排序号")
    @RequestMapping("/generateRandomSort")
    public Result generateRandomSort() {
        mediaMaterialService.generateRandomSort();
        return ResultUtils.success();
    }
}

