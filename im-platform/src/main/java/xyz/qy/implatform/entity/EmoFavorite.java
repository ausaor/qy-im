package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_emo_favorite")
public class EmoFavorite {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("emo_id")
    private Integer emoId;

    @TableField("album_id")
    private Integer albumId;

    @TableField("user_id")
    private Long userId;

    @TableField("img_url")
    private String imgUrl;

    @TableField("create_time")
    private Date createTime;
}