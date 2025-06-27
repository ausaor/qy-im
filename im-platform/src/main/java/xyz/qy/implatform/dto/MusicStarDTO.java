package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MusicStarDTO {
    @NotNull(message = "音乐id不能为空")
    private Long musicId;
}
