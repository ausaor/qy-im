package xyz.qy.implatform.controller;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
