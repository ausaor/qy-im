package xyz.qy.implatform.dto;

import lombok.Data;

@Data
public class ShortVideoLikeQueryDTO {
    private Long id;

    private Long videoId;

    private Long userId;
}
