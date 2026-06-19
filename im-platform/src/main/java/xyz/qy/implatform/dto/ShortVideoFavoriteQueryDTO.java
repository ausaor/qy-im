package xyz.qy.implatform.dto;

import lombok.Data;

@Data
public class ShortVideoFavoriteQueryDTO {
    private Long id;

    private Long videoId;

    private Long userId;
}
