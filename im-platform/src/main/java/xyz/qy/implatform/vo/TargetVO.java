package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TargetVO {
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String headImage;

    @ApiModelProperty(value = "目标id")
    private Long targetId;

    @ApiModelProperty(value = "目标类型")
    private String type;

    @ApiModelProperty(value = "关注数")
    private Integer followCount;

    @ApiModelProperty(value = "粉丝数")
    private Integer fansCount;

    @ApiModelProperty(value = "短视频获赞数")
    private Integer shortVideoLikedCount;
}
