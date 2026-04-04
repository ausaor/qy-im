package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("上传文件信息")
public class FileInfoDTO {
    private Long id;

    @ApiModelProperty("文件名称")
    private String fileName;

    @ApiModelProperty("文件类型")
    private String fileType;

    @ApiModelProperty("存储介质")
    private String storageType;
}
