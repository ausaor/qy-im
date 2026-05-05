package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 投诉记录表
 *
 * @author Lingma
 * @since 2026-05-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_complaint_record")
public class ComplaintRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 投诉目标id
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 投诉对象名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 投诉对象类型（user：用户；group：群聊；regionGroup：地区群聊）
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 投诉原因
     */
    @TableField("reason")
    private String reason;

    /**
     * 图片证据
     */
    @TableField("evidence_img")
    private String evidenceImg;

    /**
     * 投诉内容
     */
    @TableField("content")
    private String content;

    /**
     * 处理状态（0：已投诉；1：处理中；2：已处理；3：无效投诉）
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人id
     */
    @TableField("handler_user_id")
    private Long handlerUserId;

    /**
     * 操作
     */
    @TableField("operation")
    private String operation;
}