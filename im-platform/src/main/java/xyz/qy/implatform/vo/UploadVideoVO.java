package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 视频上传VO
 *
 * @author Polaris
 * @since 2025-01-01
 */
@Data
@ApiModel("视频上传VO")
public class UploadVideoVO {
    @ApiModelProperty(value = "视频链接URL")
    private String videoUrl;

    @ApiModelProperty(value = "封面图片URL")
    private String coverUrl;
}
