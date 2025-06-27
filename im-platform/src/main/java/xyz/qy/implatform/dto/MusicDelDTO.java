package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MusicDelDTO {
    @NotNull(message = "id不能为空")
    private Long id;
}
