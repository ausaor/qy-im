package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.TalkNotifyQueryDTO;
import xyz.qy.implatform.dto.TalkNotifyUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.ITalkNotifyService;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/talk-notify")
public class TalkNotifyController {
    @Resource
    private ITalkNotifyService  talkNotifyService;

    @ApiModelProperty(value = "已读动态提醒", notes = "已读动态提醒")
    @PostMapping("/readed")
    public Result readedTalkNotify(@RequestBody @Valid TalkNotifyUpdateDTO dto) {
        talkNotifyService.readedTalkNotify(dto);
        return ResultUtils.success();
    }

    @ApiModelProperty(value = "查询动态通知消息", notes = "查询动态通知消息")
    @PostMapping("/pageQueryTalkNotify")
    public Result pageQueryTalkNotify(@RequestBody @Valid TalkNotifyQueryDTO dto) {
        return ResultUtils.success(talkNotifyService.pageQueryTalkNotify(dto));
    }
}
