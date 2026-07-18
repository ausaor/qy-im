package xyz.qy.implatform.dto;

import lombok.Data;

@Data
public class ShortVideoNotifyQueryDTO {
    private Long id;

    private Long userId;

    private Long videoId;

    private Long targetId;

    private String targetType;

    private Integer actionType;

    private Boolean isRead;

    private Boolean deleted;
}
