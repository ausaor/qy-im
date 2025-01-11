package xyz.qy.implatform.vo;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @description: 动态
 * @author: Polaris
 * @create: 2023-11-20 20:49
 **/
@Data
public class TalkVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "已被选择的角色id")
    private Set<Long> selectedCharacterIds;

    @ApiModelProperty(value = "评论用户头像")
    private String commentUserAvatar;

    @ApiModelProperty(value = "评论用户昵称")
    private String commentUserNickname;

    @ApiModelProperty(value = "用户评论角色头像")
    private String commentCharacterAvatar;

    @ApiModelProperty(value = "用户评论角色昵称")
    private String commentCharacterName;

    @ApiModelProperty(value = "用户评论角色id")
    private Long commentCharacterId;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @NotBlank(message = "动态内容为空")
    @ApiModelProperty(value = "内容")
    private String content;

    @Size(max = 30, message = "最多只能上传20张图片")
    @ApiModelProperty(value = "图片URL")
    private List<String> imgUrls;

    @ApiModelProperty(value = "视频URL")
    private String videoUrl;

    @NotNull(message = "未选择公布范围")
    @ApiModelProperty(value = "公布范围")
    private Integer scope;

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

    @ApiModelProperty(value = "发布地址")
    private String address;

    @ApiModelProperty(value = "是否删除")
    private Boolean deleted;

    @ApiModelProperty(value = "是否点赞")
    private Boolean isLike = Boolean.FALSE;

    @ApiModelProperty(value = "是否自己的")
    private Boolean isOwner = Boolean.FALSE;

    @ApiModelProperty(value = "能否选择角色")
    private Boolean enableCharacterChoose = Boolean.FALSE;

    @ApiModelProperty(value = "动态赞星")
    private List<TalkStarVO> talkStarVOS;

    @ApiModelProperty(value = "动态评论")
    private List<TalkCommentVO> talkCommentVOS;

    @ApiModelProperty(value = "创建者")
    private Long createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新者")
    private Long updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    private JSONArray fileList;
}
