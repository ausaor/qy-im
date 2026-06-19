package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 短视频点赞
 *
 * @author Polaris
 * @since 2026-06-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_short_video_like")
public class ShortVideoLike extends Model<ShortVideoLike> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 短视频id
     */
    @TableField("video_id")
    private Long videoId;

    /**
     * 点赞目标用户id
     */
    @TableField("target_user_id")
    private Long targetUserId;

    /**
     * 点赞用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
