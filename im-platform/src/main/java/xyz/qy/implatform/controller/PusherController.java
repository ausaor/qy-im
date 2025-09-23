package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IPusherService;

import java.util.List;

@RestController
@RequestMapping("/pusher")
@RequiredArgsConstructor
public class PusherController {

    private final IPusherService pusherService;

    @ApiModelProperty("获取所有推送主体")
    @GetMapping("/list")
    public Result<List<Pusher>> listPusher() {
        return ResultUtils.success(pusherService.listPusher());
    }
}
