package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.vo.MediaMaterialVO;

import javax.annotation.Resource;

@Api(tags = "网站首页")
@RestController
@RequestMapping("/home")
public class HomeController {

    @Resource
    private IMediaMaterialService mediaMaterialService;

    @ApiOperation(value = "获取首页媒体播放素材", notes = "获取首页媒体播放素材")
    @PostMapping("/getPlayMediaMaterial")
    public Result getPlayMediaMaterial(@RequestBody MediaMaterialVO vo) {
        return ResultUtils.success(mediaMaterialService.getHomePlayMediaMaterial(vo));
    }
}
