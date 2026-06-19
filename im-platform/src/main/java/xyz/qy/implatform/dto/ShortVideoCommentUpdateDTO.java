package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ShortVideoCommentUpdateDTO {
    @NotNull(message = "id不能为空")
    private Long id;

    private Long videoId;

    private Long parentId;

    private Long replyToUserId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
