package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_emo_favorite")
public class EmoFavorite implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("emo_id")
    private Integer emoId;

    @TableField("album_id")
    private Integer albumId;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("image_url")
    private String imageUrl;

    @TableField("thumb_url")
    private String thumbUrl;

    @TableField("width")
    private Integer width;

    @TableField("height")
    private Integer height;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}