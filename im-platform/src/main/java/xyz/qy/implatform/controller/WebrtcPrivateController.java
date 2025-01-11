package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.OnlineCheck;
import xyz.qy.implatform.config.ICEServer;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IWebrtcPrivateService;

import java.util.List;

@Api(tags = "webrtc视频单人通话")
@RestController
@RequestMapping("/webrtc/private")
public class WebrtcPrivateController {
    @Autowired
    private IWebrtcPrivateService webrtcPrivateService;

    @ApiOperation(httpMethod = "POST", value = "呼叫视频通话")
    @PostMapping("/call")
    public Result call(@RequestParam Long uid, @RequestParam(defaultValue = "video") String mode, @RequestBody String offer) {
        webrtcPrivateService.call(uid, mode, offer);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受视频通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long uid, @RequestBody String answer) {
        webrtcPrivateService.accept(uid, answer);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "POST", value = "拒绝视频通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long uid) {
        webrtcPrivateService.reject(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消呼叫")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long uid) {
        webrtcPrivateService.cancel(uid);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "呼叫失败")
    @PostMapping("/failed")
    public Result failed(@RequestParam Long uid, @RequestParam String reason) {
        webrtcPrivateService.failed(uid, reason);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "挂断")
    @PostMapping("/handup")
    public Result handup(@RequestParam Long uid) {
        webrtcPrivateService.handup(uid);
        return ResultUtils.success();
    }


    @PostMapping("/candidate")
    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    public Result candidate(@RequestParam Long uid, @RequestBody String candidate) {
        webrtcPrivateService.candidate(uid, candidate);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "心跳")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam Long uid) {
        webrtcPrivateService.heartbeat(uid);
        return ResultUtils.success();
    }
}
