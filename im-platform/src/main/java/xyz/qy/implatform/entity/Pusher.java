package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 推送主体
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_pusher")
public class Pusher extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("head_image")
    private String headImage;
}
