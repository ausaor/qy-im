package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class ShortVideoNotifyVO {
    private Long id;

    private Long userId;

    private Long videoId;

    private Long targetId;

    private String targetType;

    private Integer actionType;

    private Boolean isRead;

    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
