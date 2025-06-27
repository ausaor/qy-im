package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;

@Data
public class MusicAddDTO {
    private String singer;

    @NotBlank(message = "音乐名称不能为空")
    private String name;

    @NotBlank(message = "音乐url不能为空")
    private String url;

    private String cover;

    @NotBlank(message = "音乐分类不能为空")
    @ValidEnum(enumClass = TalkCategoryEnum.class, property = "code", message = "无效的分类")
    private String category;

    private Long groupId;

    private String regionCode;
}
