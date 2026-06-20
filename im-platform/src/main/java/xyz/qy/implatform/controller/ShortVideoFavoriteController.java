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
import xyz.qy.implatform.dto.ShortVideoFavoriteAddDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteBatchDelDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteDelDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteQueryDTO;
import xyz.qy.implatform.dto.ShortVideoFavoriteUpdateDTO;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IShortVideoFavoriteService;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoFavoriteVO;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "短视频收藏")
@Validated
@RestController
@RequestMapping("/shortVideoFavorite")
public class ShortVideoFavoriteController {
    @Resource
    private IShortVideoFavoriteService shortVideoFavoriteService;

    @ApiOperation(value = "新增收藏", notes = "新增收藏")
    @PostMapping("/add")
    public Result<ShortVideoFavoriteVO> add(@Valid @RequestBody ShortVideoFavoriteAddDTO dto) {
        return ResultUtils.success(shortVideoFavoriteService.addShortVideoFavorite(dto));
    }

    @ApiOperation(value = "更新收藏", notes = "更新收藏")
    @PostMapping("/update")
    public Result<ShortVideoFavoriteVO> update(@Valid @RequestBody ShortVideoFavoriteUpdateDTO dto) {
        return ResultUtils.success(shortVideoFavoriteService.updateShortVideoFavorite(dto));
    }

    @ApiOperation(value = "删除收藏", notes = "删除收藏")
    @DeleteMapping("/delete")
    public Result delete(@Valid @RequestBody ShortVideoFavoriteDelDTO dto) {
        shortVideoFavoriteService.deleteShortVideoFavorite(dto);
        return ResultUtils.success();
    }

    @ApiOperation(value = "批量删除收藏", notes = "批量删除收藏")
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@Valid @RequestBody ShortVideoFavoriteBatchDelDTO dto) {
        shortVideoFavoriteService.deleteShortVideoFavorites(dto.getIds());
        return ResultUtils.success();
    }

    @ApiOperation(value = "收藏详情", notes = "收藏详情")
    @GetMapping("/detail/{id}")
    public Result<ShortVideoFavoriteVO> detail(@NotNull(message = "id不能为空") @PathVariable Long id) {
        return ResultUtils.success(shortVideoFavoriteService.getShortVideoFavoriteById(id));
    }

    @ApiOperation(value = "收藏列表", notes = "收藏列表")
    @PostMapping("/list")
    public Result<List<ShortVideoFavoriteVO>> list(@RequestBody ShortVideoFavoriteQueryDTO dto) {
        return ResultUtils.success(shortVideoFavoriteService.listShortVideoFavorites(dto));
    }

    @ApiOperation(value = "收藏分页列表", notes = "收藏分页列表")
    @PostMapping("/pageList")
    public Result<PageResultVO<List<ShortVideoFavoriteVO>>> pageList(@RequestBody ShortVideoFavoriteQueryDTO dto) {
        return ResultUtils.success(shortVideoFavoriteService.pageShortVideoFavorites(dto));
    }
}
