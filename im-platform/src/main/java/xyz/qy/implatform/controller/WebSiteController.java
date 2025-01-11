package xyz.qy.implatform.controller;

import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.service.IVisitorService;
import xyz.qy.implatform.vo.MediaMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "网站信息")
@RestController
@RequestMapping("/website")
public class WebSiteController {

    @Autowired
    private IVisitorService visitorService;

    @Autowired
    private IMediaMaterialService mediaMaterialService;

    @Autowired
    private IMusicService musicService;

    @ApiOperation(value = "上传访客信息",notes="上传访客信息")
    @PostMapping("/report")
    public Result report() {
        visitorService.report();
        return ResultUtils.success();
    }

    @ApiOperation(value = "获取媒体播放素材", notes = "获取媒体播放素材")
    @PostMapping("/getPlayMediaMaterial")
    public Result getPlayMediaMaterial(@RequestBody MediaMaterialVO vo) {
        return ResultUtils.success(mediaMaterialService.getLoginPagePlayMediaMaterial(vo));
    }

    @ApiOperation(value = "获取媒体播放素材", notes = "获取媒体播放素材")
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

    @ApiOperation(value = "爬取音乐资源", notes = "爬取音乐资源")
    @RequestMapping("/crawlMusic")
    public Result crawlMusic(Integer id) {
        musicService.crawlMusic(id);
        return ResultUtils.success();
    }
}
