package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName(value = "t_hero_skin")
@Accessors(chain = true)
public class HeroSkin {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    /**
     * 英雄id
     */
    @TableField("hero_id")
    private String heroId;

    /**
     * 英雄名称
     */
    @TableField("hero_name")
    private String heroName;

    /**
     * 皮肤名称
     */
    @TableField("skin_name")
    private String skinName;

    /**
     * 皮肤图片链接
     */
    @TableField("skin_url")
    private String skinUrl;

    /**
     * 皮肤头像链接
     */
    @TableField("skin_profile_url")
    private String skinProfileUrl;

    /**
     * 类别
     */
    @TableField("category")
    private String category;

    /**
     * 是否爬取
     */
    @TableField("is_crawl")
    private Integer isCrawl;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;
}
