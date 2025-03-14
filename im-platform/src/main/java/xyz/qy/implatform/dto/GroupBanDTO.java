package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class GroupBanDTO {
    /**
     * 群聊id
     */
    @NotNull(message = "参数异常")
    private Long id;


    /**
     * 用户id
     */
    private Long userId;


    /**
     * 禁言类型
     */
    @NotBlank(message = "禁言类型为空")
    private String banType;

    /**
     * 禁言时长
     */
    @Min(value = 1, message = "禁言时长最短1分钟")
    @NotNull(message = "禁言时长不能为空")
    private Integer banDuration;

    /**
     * 是否全局禁言
     */
    private Boolean allBanned = false;
}
