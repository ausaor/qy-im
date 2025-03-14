package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xyz.qy.implatform.config.WebrtcConfig;
import xyz.qy.implatform.dto.WebrtcGroupAnswerDTO;
import xyz.qy.implatform.dto.WebrtcGroupCandidateDTO;
import xyz.qy.implatform.dto.WebrtcGroupDeviceDTO;
import xyz.qy.implatform.dto.WebrtcGroupFailedDTO;
import xyz.qy.implatform.dto.WebrtcGroupInviteDTO;
import xyz.qy.implatform.dto.WebrtcGroupOfferDTO;
import xyz.qy.implatform.dto.WebrtcGroupSetupDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IWebrtcGroupService;
import xyz.qy.implatform.vo.WebrtcGroupInfoVO;

import javax.validation.Valid;

@Api(tags = "webrtc视频多人通话")
@RestController
@RequestMapping("/webrtc/group")
@RequiredArgsConstructor
public class WebrtcGroupController {

    private final IWebrtcGroupService webrtcGroupService;

    @ApiOperation(httpMethod = "POST", value = "发起群视频通话")
    @PostMapping("/setup")
    public Result setup(@Valid @RequestBody WebrtcGroupSetupDTO dto) {
        webrtcGroupService.setup(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "接受通话")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long groupId) {
        webrtcGroupService.accept(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "拒绝通话")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long groupId) {
        webrtcGroupService.reject(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "通话失败")
    @PostMapping("/failed")
    public Result failed(@Valid @RequestBody WebrtcGroupFailedDTO dto) {
        webrtcGroupService.failed(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "进入视频通话")
    @PostMapping("/join")
    public Result join(@RequestParam Long groupId) {
        webrtcGroupService.join(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "取消通话")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long groupId) {
        webrtcGroupService.cancel(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "离开视频通话")
    @PostMapping("/quit")
    public Result quit(@RequestParam Long groupId) {
        webrtcGroupService.quit(groupId);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "推送offer信息")
    @PostMapping("/offer")
    public Result offer(@Valid @RequestBody WebrtcGroupOfferDTO dto) {
        webrtcGroupService.offer(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "推送answer信息")
    @PostMapping("/answer")
    public Result answer(@Valid @RequestBody WebrtcGroupAnswerDTO dto) {
        webrtcGroupService.answer(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "邀请用户进入视频通话")
    @PostMapping("/invite")
    public Result invite(@Valid @RequestBody WebrtcGroupInviteDTO dto) {
        webrtcGroupService.invite(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "同步candidate")
    @PostMapping("/candidate")
    public Result candidate(@Valid @RequestBody WebrtcGroupCandidateDTO dto) {
        webrtcGroupService.candidate(dto);
        return ResultUtils.success();
    }

    @ApiOperation(httpMethod = "POST", value = "设备操作")
    @PostMapping("/device")
    public Result device(@Valid @RequestBody WebrtcGroupDeviceDTO dto) {
        webrtcGroupService.device(dto);
        return ResultUtils.success();
    }


    @ApiOperation(httpMethod = "GET", value = "获取通话信息")
    @GetMapping("/info")
    public Result<WebrtcGroupInfoVO> info(@RequestParam Long groupId) {
        return ResultUtils.success(webrtcGroupService.info(groupId));
    }

    @ApiOperation(httpMethod = "POST", value = "心跳")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam Long groupId) {
        webrtcGroupService.heartbeat(groupId);
        return ResultUtils.success();
    }
}

