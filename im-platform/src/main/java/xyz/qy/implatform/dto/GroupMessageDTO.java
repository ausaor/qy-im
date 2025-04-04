package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("群聊消息VO")
public class GroupMessageDTO {

    @NotNull(message="群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @Length(max=1024,message = "发送内容长度不得大于1024")
    @NotEmpty(message="发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message="消息类型不可为空")
    @ApiModelProperty(value = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频")
    private Integer type;

    @ApiModelProperty(value = "是否回执消息")
    private Boolean receipt = false;

    @Size(max = 20,message = "一次最多只能@20个小伙伴哦")
    @ApiModelProperty(value = "被@用户列表")
    private List<Long> atUserIds;

    @ApiModelProperty(value = "引用消息id")
    private Long quoteId;
}
