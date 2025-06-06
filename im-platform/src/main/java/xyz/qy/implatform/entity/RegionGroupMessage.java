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
 * 地区群聊消息
 *
 * @author Polaris
 * @since 2022-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_region_group_message")
public class RegionGroupMessage extends Model<RegionGroupMessage> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 地区群聊id
     */
    @TableField("region_group_id")
    private Long regionGroupId;

    /**
     * 发送用户id
     */
    @TableField("send_id")
    private Long sendId;

    /**
     * 发送用户昵称
     */
    @TableField("send_nick_name")
    private String sendNickName;

    /**
     * 接受用户id,为空表示全体发送
     */
    @TableField("recv_ids")
    private String recvIds;

    /**
     * @用户列表
     */
    @TableField("at_user_ids")
    private String atUserIds;

    /**
     * 发送内容
     */
    @TableField("content")
    private String content;

    /**
     * 引用消息id
     */
    @TableField("quote_id")
    private Long quoteId;

    /**
     * 引用消息内容
     */
    @TableField("quote_msg")
    private String quoteMsg;

    /**
     * 消息类型 0:文字 1:图片 2:文件
     */
    @TableField("type")
    private Integer type;

    /**
     *  是否回执消息
     */
    @TableField("receipt")
    private Boolean receipt;

    /**
     *  回执消息是否完成
     */
    @TableField("receipt_ok")
    private Boolean receiptOk;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private Date sendTime;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
