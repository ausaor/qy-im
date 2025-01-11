package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-05-02 09:20
 **/
@Data
@ApiModel("登录页展示媒体素材VO")
public class MediaMaterialVO {
    @ApiModelProperty(value = "素材名称")
    private String title;

    @ApiModelProperty(value = "素材简介")
    private String description;

    @ApiModelProperty(value = "素材链接")
    private String url;

    @ApiModelProperty(value = "封面图片")
    private String coverImage;

    @ApiModelProperty(value = "素材类型")
    private String type;

    @ApiModelProperty(value = "格式")
    private String format;

    @ApiModelProperty(value = "素材展示时长（单位秒）")
    private Integer displayDuration;

    @ApiModelProperty(value = "排序号")
    private Integer sort;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "素材展示开始时间")
    private Date startTime;

    @ApiModelProperty(value = "素材展示结束时间")
    private Date endTime;

    @ApiModelProperty(value = "是否展示媒体素材")
    private Boolean showMedia = false;
}
