package xyz.qy.implatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.dto.ShortVideoNotifyAddDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyBatchDelDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyQueryDTO;
import xyz.qy.implatform.dto.ShortVideoNotifyUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoNotifyService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoNotifyVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "短视频互动消息通知")
@Validated
@RestController
@RequestMapping("/shortVideoNotify")
public class ShortVideoNotifyController {

    @Resource
    private IShortVideoNotifyService shortVideoNotifyService;

    @ApiOperation(value = "新增短视频通知", notes = "新增短视频通知")
    @PostMapping("/add")
    public Result<ShortVideoNotifyVO> add(@Valid @RequestBody ShortVideoNotifyAddDTO dto) {
        return ResultUtils.success(shortVideoNotifyService.addShortVideoNotify(dto));
    }

    @ApiOperation(value = "查询短视频通知详情", notes = "查询短视频通知详情")
    @GetMapping("/get/{id}")
    public Result<ShortVideoNotifyVO> get(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        return ResultUtils.success(shortVideoNotifyService.getShortVideoNotify(id));
    }

    @ApiOperation(value = "查询短视频通知列表", notes = "查询短视频通知列表")
    @PostMapping("/list")
    public Result<List<ShortVideoNotifyVO>> list(@RequestBody ShortVideoNotifyQueryDTO dto) {
        return ResultUtils.success(shortVideoNotifyService.listShortVideoNotify(dto));
    }

    @ApiOperation(value = "分页查询短视频通知", notes = "分页查询短视频通知")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoNotifyVO>>> pageList(@RequestBody ShortVideoNotifyQueryDTO dto) {
        return ResultUtils.success(shortVideoNotifyService.pageShortVideoNotify(dto));
    }

    @ApiOperation(value = "更新短视频通知", notes = "更新短视频通知")
    @PostMapping("/update")
    public Result<ShortVideoNotifyVO> update(@Valid @RequestBody ShortVideoNotifyUpdateDTO dto) {
        return ResultUtils.success(shortVideoNotifyService.updateShortVideoNotify(dto));
    }

    @ApiOperation(value = "删除短视频通知", notes = "删除短视频通知")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") @NotNull(message = "id不能为空") Long id) {
        shortVideoNotifyService.deleteShortVideoNotify(id);
        return ResultUtils.success();
    }

    @ApiOperation(value = "批量删除短视频通知", notes = "批量删除短视频通知")
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@Valid @RequestBody ShortVideoNotifyBatchDelDTO dto) {
        shortVideoNotifyService.batchDeleteShortVideoNotify(dto.getIds());
        return ResultUtils.success();
    }
}
