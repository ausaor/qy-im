package xyz.qy.implatform.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.imcommon.serializer.DateToLongSerializer;

import java.util.Date;

@Data
@ApiModel("系统消息VO")
public class SystemMessageVO {
    @ApiModelProperty(value = "推送消息的任务id")
    private Long seqNo;

    @ApiModelProperty(value = "系统消息id")
    private Long id;

    @ApiModelProperty(value = "推送者id")
    private Long pusherId;

    @ApiModelProperty(value = "推送者名称")
    private String pusherName;

    @ApiModelProperty(value = "推送者头像")
    private String pusherHeadImage;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "封面图片")
    private String coverUrl;

    @ApiModelProperty(value = "简述")
    private String intro;

    @ApiModelProperty(value = "外链")
    private String externLink;

    @ApiModelProperty(value = " 发送内容")
    private String content;

    @ApiModelProperty(value = " 状态")
    private Integer status;

    @ApiModelProperty(value = "消息内容类型 MessageType")
    private Integer type;

    @ApiModelProperty(value = " 发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
