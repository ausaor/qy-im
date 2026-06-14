package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import xyz.qy.implatform.enums.SectionEnum;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @description: 动态查询 DTO
 * @author: Polaris
 * @create: 2023-11-20 20:09
 **/
@Data
public class TalkQueryDTO {
    @ApiModelProperty(value = "可见范围")
    private Integer scope;

    @ApiModelProperty(value = "作者id")
    private Long ownerId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "好友id")
    private List<Long> friendIds;

    @ApiModelProperty(value = "群友id")
    private List<Long> groupMemberUserIds;

    @ApiModelProperty(value = "分类")
    @NotBlank(message = "未选择分类")
    @ValidEnum(enumClass = TalkCategoryEnum.class, property = "code", message = "无效的分类")
    private String category;

    @ApiModelProperty("动态会话")
    @ValidEnum(enumClass = SectionEnum.class, property = "code", message = "无效的分类")
    private String section;

    @ApiModelProperty("群id")
    private Long groupId;

    @ApiModelProperty("地区群聊id")
    private Long regionGroupId;

    @ApiModelProperty("地区编码")
    private String regionCode;

    @ApiModelProperty("角色id")
    private Long characterId;

    @ApiModelProperty("群聊模板id")
    private Long groupTemplateId;

    @ApiModelProperty("群聊模板id列表")
    private List<Long> templateGroupIds;

    @ApiModelProperty("角色绑定的用户id")
    private List<Long> characterUserIds;

    @Size(max = 500)
    @ApiModelProperty("角色id列表")
    private List<Long> characterIds;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("状态列表")
    private List<String> statusList;
}
