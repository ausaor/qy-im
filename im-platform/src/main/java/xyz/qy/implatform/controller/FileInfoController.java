package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.annotation.RequireRoles;
import xyz.qy.implatform.dto.FileInfoDTO;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IFileInfoService;

/**
 * 上传文件信息表 Controller
 *
 * @author Polaris
 * @since 2026-04-02
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileInfoController {

    private final IFileInfoService fileInfoService;

    @ApiOperation(value = "分页查询文件信息", notes = "分页查询文件信息")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @PostMapping("/page")
    public Result pageFileInfo(@RequestBody FileInfoDTO fileInfoDTO) {
        return ResultUtils.success(fileInfoService.pageFileInfo(fileInfoDTO));
    }

    @ApiOperation(value = "删除文件信息", notes = "删除文件信息")
    @RequireRoles(value = {RoleEnum.SUPER_ADMIN})
    @PostMapping("/delete")
    public Result deleteFileInfo(@RequestBody FileInfoDTO fileInfoDTO) {
        fileInfoService.deleteFileInfo(fileInfoDTO);
        return ResultUtils.success();
    }
}
