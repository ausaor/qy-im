package xyz.qy.implatform.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ShortVideoCommentQueryDTO {
    @NotNull
    private Long videoId;

    private Long userId;

    private Long topReplyCommentId;
}
