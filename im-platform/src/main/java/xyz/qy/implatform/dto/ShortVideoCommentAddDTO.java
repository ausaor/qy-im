package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShortVideoCommentAddDTO {
    @NotNull(message = "视频id不能为空")
    private Long videoId;

    @NotBlank(message = "评论内容不能为空")
    @Length(min = 1, max = 200, message = "评论内容长度不能超过200")
    private String content;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "角色头像id")
    private Long avatarId;

    @ApiModelProperty(value = "回复的评论id")
    private Long replyCommentId;

    @ApiModelProperty(value = "评论类型 0:文字 1:图片 5:语音台词")
    @NotNull(message = "评论类型不能为空")
    private Integer type;
}
