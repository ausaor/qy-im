package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RegionGroupBanDTO {
    /**
     * 地区群聊id
     */
    @NotNull(message = "参数异常")
    private Long id;

    /**
     * 地区编码
     */
    @NotBlank(message = "地区编码不能为空")
    private String code;

    /**
     * 地区群聊编号
     */
    @NotBlank(message = "地区群聊编号不能为空")
    private String num;

    /**
     * 加入地区群聊方式：0-临时，1-常驻
     */
    @Max(value = 1, message = "加入方式只能是临时或常驻")
    @Min(value = 0, message = "加入方式只能是临时或常驻")
    private Integer joinType;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String aliasName;

    /**
     * 禁言类型
     */
    @NotBlank(message = "禁言类型为空")
    private String banType;

    /**
     * 禁言时长
     */
    @Min(value = 1, message = "禁言时长最短1小时")
    @NotNull(message = "禁言时长不能为空")
    private Integer banDuration;

    /**
     * 是否全局禁言
     */
    private Boolean allBanned = false;
}
