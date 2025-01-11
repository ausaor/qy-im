package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统消息
 *
 * @author Polaris
 * @since 2022-11-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_system_message")
public class SystemMessage extends BaseEntity{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("intro")
    private String intro;

    @TableField("content")
    private String content;

    @TableField("extern_link")
    private String externLink;

    @TableField("type")
    private Integer type;
}
