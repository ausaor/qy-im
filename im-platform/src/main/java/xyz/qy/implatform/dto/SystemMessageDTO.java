package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 系统消息DTO
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Data
@ApiModel("系统消息DTO")
public class SystemMessageDTO {
    @ApiModelProperty(value = "系统消息id")
    private Long id;

    @ApiModelProperty(value = "标题")
    @NotBlank(message = "消息标题为空")
    private String title;

    private String coverUrl;

    private String intro;

    @NotBlank(message = "消息内容为空")
    private String content;

    private String externLink;

    @NotNull(message = "消息类型为空")
    private Integer type;
}
