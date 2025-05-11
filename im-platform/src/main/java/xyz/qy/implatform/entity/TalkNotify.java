package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("im_talk_notify")
public class TalkNotify {
    @TableId
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("taik_id")
    private Long taikId;

    @TableField("action_type")
    private Integer actionType;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("create_time")
    private LocalDateTime createTime;
}
