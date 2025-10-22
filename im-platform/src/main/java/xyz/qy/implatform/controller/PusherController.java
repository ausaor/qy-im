package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IPusherService;

import java.util.List;

@RestController
@RequestMapping("/pusher")
@RequiredArgsConstructor
public class PusherController {

    private final IPusherService pusherService;

    @ApiOperation(value = "获取所有推送主体")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @GetMapping("/list")
    public Result<List<Pusher>> listPusher() {
        return ResultUtils.success(pusherService.listPusher());
    }
}
