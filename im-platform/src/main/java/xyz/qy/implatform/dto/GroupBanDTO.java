package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("群禁言DTO")
public class GroupBanDTO {
    /**
     * 群聊id
     */
    @NotNull(message = "参数异常")
    @ApiModelProperty(value = "群聊id")
    private Long id;


    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;


    @ApiModelProperty(value = "用户id列表")
    private List<Long> userIds;


    /**
     * 禁言类型
     */
    @NotBlank(message = "禁言类型为空")
    @ApiModelProperty(value = "禁言类型")
    private String banType;

    /**
     * 禁言时长
     */
    @Min(value = -1, message = "禁言时长不能小于-1")
    @Max(value = 60000, message = "禁言时长不能大于60000")
    @NotNull(message = "禁言时长不能为空")
    @ApiModelProperty(value = "禁言时长")
    private Integer banDuration;

    /**
     * 是否全局禁言
     */
    @ApiModelProperty(value = "是否全局禁言")
    private Boolean allBanned = false;
}
