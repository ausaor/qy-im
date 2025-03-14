package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 动态删除DTO
 * @author: Polaris
 * @create: 2023-12-23 09:19
 **/
@Data
public class TalkDelDTO {
    @ApiModelProperty(value = "动态id")
    @NotNull(message = "参数异常")
    private Long id;
}
