package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class ShortVideoMessageVO {
    private Long userId;

    private Long videoId;

    private Long targetId;

    private String targetType;

    private Integer actionType;
}
