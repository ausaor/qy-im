package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShortVideoNotifyAddDTO {
    @NotNull(message = "接收提醒的用户ID不能为空")
    private Long userId;

    @NotNull(message = "短视频id不能为空")
    private Long videoId;

    @NotNull(message = "目标id不能为空")
    private Long targetId;

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "提醒类型不能为空")
    private Integer actionType;

    private Boolean isRead;
}
