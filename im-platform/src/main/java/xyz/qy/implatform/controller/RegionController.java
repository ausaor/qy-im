package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IRegionService;
import xyz.qy.implatform.vo.RegionVO;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.util.List;


/**
 * 地区Controller
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Api(tags = "地区")
@Validated
@RequestMapping("/region")
@RestController
public class RegionController {
    @Resource
    private IRegionService regionService;

    @ApiOperation(value = "根据上级地区编码查询地区列表", notes = "根据上级地区编码查询地区列表")
    @GetMapping("/findSubRegion")
    public Result<List<RegionVO>> findRegionByParentCode(String parentCode) {
        return ResultUtils.success(regionService.findRegionByParentCode(parentCode));
    }

    @ApiOperation(value = "获取地区经纬度", notes = "获取地区经纬度")
    @GetMapping("/getRegionLonAndLat")
    public Result<RegionVO> getRegionLonAndLat(@NotBlank(message = "地区编码为空") String code) {
        return ResultUtils.success(regionService.getRegionLonAndLat(code));
    }

    @ApiOperation(value = "获取活跃地区", notes = "获取活跃地区")
    @GetMapping("/findActivityRegions")
    public Result<List<RegionVO>> findActivityRegions() {
        return ResultUtils.success(regionService.findActivityRegions());
    }
}
