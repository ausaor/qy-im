package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 请求添加好友表
 *
 * @author Polaris
 * @since 2025-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_friend_request")
public class FriendRequest extends Model<FriendRequest> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发送者id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 发送者头像
     */
    @TableField("send_head_image")
    private String sendHeadImage;

    /**
     * 发送者昵称
     */
    @TableField("send_nick_name")
    private String sendNickName;

    /**
     * 接收者id
     */
    @TableField("recv_id")
    private Long recvId;

    /**
     * 接收者头像
     */
    @TableField("recv_head_image")
    private String recvHeadImage;

    /**
     * 接收者昵称
     */
    @TableField("recv_nick_name")
    private String recvNickName;

    /**
     * 状态（0：撤回；1：申请；2：同意；3：拒绝）
     */
    @TableField("status")
    private Integer status;

    /**
     * 申请时间
     */
    @TableField("apply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 申请备注
     */
    @TableField("remark")
    private String remark;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
