package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShortVideoNotifyUpdateDTO {
    @NotNull(message = "id不能为空")
    private Long id;

    private Long userId;

    private Long videoId;

    private Long targetId;

    private String targetType;

    private Integer actionType;

    private Boolean isRead;

    private Boolean deleted;
}
