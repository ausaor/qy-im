package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 模板人物
 * @author: Polaris
 * @create: 2023-03-12 16:24
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_template_character")
public class TemplateCharacter implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 模板群聊id */
    @TableField("template_group_id")
    private Long templateGroupId;

    /** 人物名称 */
    @TableField("name")
    private String name;

    /** 人物描述 */
    @TableField("description")
    private String description;

    /** 人物头像 */
    @TableField("avatar")
    private String avatar;


    /** 是否删除：0-否；1-是 */
    @TableField("deleted")
    private Boolean deleted;

    /** 状态 */
    @TableField("status")
    private String status;

    /** 创建者 */
    @TableField("create_by")
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新者 */
    @TableField("update_by")
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
