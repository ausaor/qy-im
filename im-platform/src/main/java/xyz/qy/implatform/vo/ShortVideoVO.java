package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShortVideoVO {
    private Long id;

    private Long userId;

    private Long objectId;

    private String category;

    private Integer scope;

    private String title;

    private String description;

    private String videoUrl;

    private String coverUrl;

    private Integer duration;

    private Integer width;

    private Integer height;

    private Long size;

    private Integer playCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer commentCount;

    private String status;

    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private Boolean isOwner = Boolean.FALSE;
}
