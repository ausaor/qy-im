package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @description: 模板人物头像
 * @author: Polaris
 * @create: 2023-06-10 16:11
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_character_avatar")
public class CharacterAvatar extends Model<CharacterAvatar> {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板人物id
     */
    @TableField("template_character_id")
    private Long templateCharacterId;

    /**
     * 模板人物名称
     */
    @TableField("template_character_name")
    private String templateCharacterName;

    /**
     * 头像名称
     */
    @TableField("name")
    private String name;

    /**
     * 头像链接
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 头像等级
     */
    @TableField("level")
    private Integer level;

    /** 是否删除 */
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
