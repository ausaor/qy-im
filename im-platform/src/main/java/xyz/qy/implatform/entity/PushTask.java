package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 消息推送任务
 *
 * @author Polaris
 * @since 2022-11-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_push_task")
public class PushTask extends Model<PushTask> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "seq_no", type = IdType.AUTO)
    private Long seqNo;

    @TableField("pusher_id")
    private Long pusherId;

    @TableField("recv_range")
    private String recvRange;

    @TableField("sys_msg_id")
    private Long sysMsgId;

    @TableField("title")
    private String title;

    @TableField("send_time")
    private Date sendTime;

    @TableField("push_status")
    private Integer pushStatus;

    @TableField("status")
    private Integer status;

    @TableField("create_by")
    private Long createBy;

    @TableField("create_time")
    private Date createTime;

    @TableField("deleted")
    private Boolean deleted;
}
