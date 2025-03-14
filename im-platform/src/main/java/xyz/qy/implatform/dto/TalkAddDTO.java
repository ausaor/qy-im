package xyz.qy.implatform.dto;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @description: 动态新增DTO
 * @author: Polaris
 * @create: 2023-11-19 21:45
 **/
@Data
public class TalkAddDTO {
    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "角色人物id")
    private Long characterId;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @NotBlank(message = "动态内容为空")
    @ApiModelProperty(value = "内容")
    private String content;

    @Size(max = 30, message = "最多只能上传30张图片")
    @ApiModelProperty(value = "图片URL")
    private List<String> imgUrls;

    @ApiModelProperty(value = "视频URL")
    private String videoUrl;

    @NotNull(message = "未选择公布范围")
    @ApiModelProperty(value = "公布范围")
    private Integer scope;

    @ApiModelProperty(value = "发布地址")
    private String address;

    @ApiModelProperty(value = "分类（private：个人，group：群聊，region：地区）")
    private String category;

    @ApiModelProperty(value = "群聊空间可见")
    private Boolean groupVisible;

    @ApiModelProperty(value = "地区空间可见")
    private Boolean regionVisible;

    @ApiModelProperty(value = "群id")
    private Long groupId;

    @ApiModelProperty(value = "地区编码")
    private String regionCode;

    @ApiModelProperty(value = "文件列表")
    private JSONArray files;
}
