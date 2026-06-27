package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.enums.FollowEnum;
import xyz.qy.implatform.enums.ValidEnum;
import xyz.qy.implatform.enums.ViewScopeEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShortVideoUpdateDTO {
    @NotNull(message = "id不能为空")
    private Long id;

    private Long objectId;

    @NotBlank(message = "分类不能为空")
    @ValidEnum(enumClass = FollowEnum.class, property = "code", message = "无效的分类")
    private String type;

    @NotNull(message = "未选择公布范围")
    @ValidEnum(enumClass = ViewScopeEnum.class, property = "code", message = "无效的公布范围")
    private Integer scope;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "视频地址不能为空")
    private String videoUrl;

    @NotBlank(message = "封面地址不能为空")
    private String coverUrl;

    private Integer duration;

    private Integer width;

    private Integer height;

    private Long size;

    private String status;
}
