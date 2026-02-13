package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_emo_img")
public class EmoImg extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("album_id")
    private Integer albumId;

    @TableField("name")
    private String name;

    @TableField("img_url")
    private String imgUrl;

    @TableField("width")
    private Integer width;

    @TableField("height")
    private Integer height;
}