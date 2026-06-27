package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShortVideoVO {
    private Long id;

    private Long userId;

    private String nickName;

    private String authorName;

    private String headImage;

    private Long objectId;

    private String type;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;

    private Boolean isOwner = Boolean.FALSE;

    private Boolean liked = Boolean.FALSE;

    private Boolean favorited = Boolean.FALSE;

    private Boolean followed = Boolean.FALSE;
}
