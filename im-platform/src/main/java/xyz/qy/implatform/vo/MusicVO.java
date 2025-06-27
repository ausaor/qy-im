package xyz.qy.implatform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MusicVO {
    private Long id;

    private Long userId;

    private String singer;

    private String name;

    private String url;

    private String cover;

    private Integer duration;

    private Integer playCount;

    private String category;

    private Long groupId;

    private String regionCode;

    private Boolean deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer likeCount;

    private Boolean liked;
}
