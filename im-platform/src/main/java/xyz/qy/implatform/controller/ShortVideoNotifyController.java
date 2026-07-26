package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoNotifyService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "短视频互动消息通知")
@Validated
@RestController
@RequestMapping("/shortVideoNotify")
public class ShortVideoNotifyController {

    @Resource
    private IShortVideoNotifyService shortVideoNotifyService;

    @ApiOperation(value = "已读短视频通知", notes = "已读短视频通知")
    @PostMapping("/readed")
    public Result readedShortVideoNotify(@RequestParam Long targetId, @RequestParam String targetType) {
        shortVideoNotifyService.readedShortVideoNotify(targetId, targetType);
        return ResultUtils.success();
    }

    @ApiOperation(value = "已读所有短视频通知", notes = "已读所有短视频通知")
    @PostMapping("/readedAll")
    public Result readedAllShortVideoNotify() {
        shortVideoNotifyService.readedAllShortVideoNotify();
        return ResultUtils.success();
    }

    @ApiOperation(value = "分页查询短视频通知", notes = "分页查询短视频通知")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoNotifyVO>>> pageList(@RequestBody ShortVideoNotifyQueryDTO dto) {
        return ResultUtils.success(shortVideoNotifyService.pageShortVideoNotify(dto));
    }

    @ApiOperation(value = "拉取离线通知", notes = "拉取离线通知")
    @GetMapping("/pullOfflineNotify")
    public Result pullOfflineNotify() {
        shortVideoNotifyService.pullOfflineNotify();
        return ResultUtils.success();
    }
}
