package xyz.qy.implatform.dto;

import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.enums.ValidEnum;
import xyz.qy.implatform.enums.ViewScopeEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-12-23 16:02
 **/
@Data
public class TalkUpdateDTO {
    @ApiModelProperty("主键")
    @NotNull(message = "参数异常")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "群模板id")
    private Long groupTemplateId;

    @ApiModelProperty(value = "角色人物id")
    private Long characterId;

    @ApiModelProperty(value = "头像id")
    private Long avatarId;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @Length(min = 1, max = 200, message = "动态内容长度不能超过200")
    @NotBlank(message = "动态内容为空")
    @ApiModelProperty(value = "内容")
    private String content;

    @Size(max = 30, message = "最多只能上传30张图片")
    @ApiModelProperty(value = "图片URL")
    private List<String> imgUrls;

    @ApiModelProperty(value = "视频URL")
    private String videoUrl;

    @ValidEnum(enumClass = ViewScopeEnum.class, property = "code", message = "无效的公布范围")
    @NotNull(message = "未选择公布范围")
    @ApiModelProperty(value = "公布范围")
    private Integer scope;

    @ApiModelProperty(value = "发布地址")
    private String address;

    @ApiModelProperty(value = "分类（private：个人，group：群聊，region：地区）")
    @NotBlank(message = "未选择分类")
    @ValidEnum(enumClass = TalkCategoryEnum.class, property = "code", message = "无效的分类")
    private String category;

    @ApiModelProperty(value = "群聊空间可见")
    private Boolean groupVisible;

    @ApiModelProperty(value = "地区空间可见")
    private Boolean regionVisible;

    @ApiModelProperty(value = "角色空间可见")
    private Boolean characterVisible;

    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty(value = "地区编码")
    private String regionCode;

    @ApiModelProperty(value = "文件列表")
    @Size(max = 9, message = "最多只能上传9个文件")
    private JSONArray files;
}
