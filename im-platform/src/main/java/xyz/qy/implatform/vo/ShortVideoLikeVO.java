package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShortVideoLikeVO {
    private Long id;

    private Long videoId;

    private Long userId;

    private String nickName;

    private String headImage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private Boolean isOwner = Boolean.FALSE;
}
