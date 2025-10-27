package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 地区群聊消息
 *
 * @author Polaris
 * @since 2022-11-03
 */
@Data
@ApiModel("地区群聊消息VO")
public class RegionGroupMessageDTO {
    @ApiModelProperty(value = "地区编码")
    //@NotNull(message = "地区编码不可为空")
    private String code;

    /**
     * 临时id,由前端生成
     * 作用:如果用户正在发送消息时掉线了，可以通过此字段获取该消息的实际发送状态
     */
    @ApiModelProperty(value = "临时id")
    private String tmpId;

    @NotNull(message = "地区群聊id不可为空")
    @ApiModelProperty(value = "地区群聊id")
    private Long regionGroupId;

    @Length(max = 1024, message = "发送内容长度不得大于1024")
    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message = "消息类型不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

    @Size(max = 20, message = "一次最多只能@20个小伙伴哦")
    @ApiModelProperty(value = "被@用户列表")
    private List<Long> atUserIds;

    @ApiModelProperty(value = "加入类型")
    @NotNull(message = "加入方式不能为空")
    @Max(value = 1, message = "加入方式只能是临时或常驻")
    @Min(value = 0, message = "加入方式只能是临时或常驻")
    private Integer joinType;

    @ApiModelProperty(value = "是否回执消息")
    private Boolean receipt = false;

    @ApiModelProperty(value = "引用消息id")
    private Long quoteId;
}
