package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @description: 记录用户群聊消息的已读位置表
 * @author: Polaris
 * @create: 2023-09-09 20:01
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_group_msg_read_position")
public class GroupMsgReadPosition extends Model<GroupMsgReadPosition> {
    @TableField(value = "group_id")
    private Long groupId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "group_msg_id")
    private Long groupMsgId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
