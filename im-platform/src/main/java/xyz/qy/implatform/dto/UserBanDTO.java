package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBanDTO {
    @NotNull(message = "用户id不能为空")
    private Long userId;

    private Integer banDuration;
}
