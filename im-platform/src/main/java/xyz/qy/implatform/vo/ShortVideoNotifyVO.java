package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    private Long operateUserId;

    private String operateUserNickname;

    private String operateUserHeadImage;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    private Integer recordType;

    private Boolean deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private ShortVideoVO shortVideo;

    private ShortVideoCommentVO shortVideoComment;
}
