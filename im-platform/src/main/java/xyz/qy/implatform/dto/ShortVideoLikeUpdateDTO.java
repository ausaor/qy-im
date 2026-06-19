package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShortVideoLikeUpdateDTO {
    @NotNull(message = "id不能为空")
    private Long id;

    @NotNull(message = "视频id不能为空")
    private Long videoId;
}
