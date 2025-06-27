package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;

@Data
public class MusicQueryDTO {
    @NotBlank(message = "音乐分类不能为空")
    @ValidEnum(enumClass = TalkCategoryEnum.class, property = "code", message = "无效的分类")
    private String category;

    @ApiModelProperty("查询范围：my-自己的，friend-朋友的，group-群聊的，region-地区的；")
    private String section;

    private Long friendId;

    private Long groupId;

    private String regionCode;
}
