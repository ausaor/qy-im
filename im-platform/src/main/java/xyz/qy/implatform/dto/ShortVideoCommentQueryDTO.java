package xyz.qy.implatform.dto;

import lombok.Data;

@Data
public class ShortVideoCommentQueryDTO {
    private Long id;

    private Long videoId;

    private Long userId;

    private Long parentId;

    private Long replyToUserId;

    private String content;
}
