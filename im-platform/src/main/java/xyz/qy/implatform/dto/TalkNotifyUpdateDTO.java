package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("群聊通知更新DTO")
public class TalkNotifyUpdateDTO {
    @NotBlank(message = "分类不能为空")
    @ValidEnum(enumClass = TalkCategoryEnum.class, property = "code", message = "无效的分类")
    private String category;

    private Long groupId;

    private String regionCode;

    private Long groupTemplateId;

    private Long characterId;
}
