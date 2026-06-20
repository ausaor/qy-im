package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShortVideoLikeDelDTO {
    @NotNull(message = "videoId不能为空")
    private Long videoId;
}
