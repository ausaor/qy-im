package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
    private String content;

    @ApiModelProperty(value = "用户昵称")
    private String userNickname;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "角色id")
    private Long characterId;

    @ApiModelProperty(value = "回复的评论id")
    private Long replyCommentId;
}
