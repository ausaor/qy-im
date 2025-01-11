package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 推送任务DTO
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Data
@ApiModel("推送任务DTO")
public class PushTaskDTO {
    @NotNull(message = "推送主体为空")
    private Long pusherId;

    @NotNull(message = "系统消息id为空")
    private Long sysMsgId;

    @NotBlank(message = "接收者范围为空")
    private String recvRange;

    @NotBlank(message = "消息标题为空")
    private String title;

    private Date sendTime;

    @ApiModelProperty(value = "接收用户id")
    private List<Long> recvUserIds;
}
