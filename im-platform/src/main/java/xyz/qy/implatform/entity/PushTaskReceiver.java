package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息推送任务的接收人
 *
 * @author Polaris
 * @since 2022-11-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_push_task_receiver")
public class PushTaskReceiver {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("seq_no")
    private Long seqNo;

    @TableField("user_id")
    private Long userId;
}
