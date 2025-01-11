package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
 * @description: 音乐
 * @author: Polaris
 * @create: 2023-07-16 15:38
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_music")
public class Music extends Model<Music> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("origin_id")
    private String originId;

    @TableField("singer")
    private String singer;

    @TableField("music_name")
    private String musicName;

    @TableField("music_url")
    private String musicUrl;

    @TableField("has_crawl")
    private Integer hasCrawl;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
