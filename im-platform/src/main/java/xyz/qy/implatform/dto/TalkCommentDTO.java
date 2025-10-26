package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @description: 动态评论
 * @author: Polaris
 * @create: 2024-01-06 09:05
 **/
@Data
public class TalkCommentDTO {
    @ApiModelProperty(value = "动态id")
    @NotNull(message = "参数异常")
    private Long talkId;

    @ApiModelProperty(value = "评论内容")
    @NotNull(message = "评论内容不能为空")
    @Length(min = 1, max = 100, message = "评论内容长度不能超过100")
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
