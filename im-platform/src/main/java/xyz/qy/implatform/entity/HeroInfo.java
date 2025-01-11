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
@TableName(value = "t_hero_info")
@Accessors(chain = true)
public class HeroInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 英雄名称
     */
    @TableField("hero_name")
    private String heroName;

    /**
     * 英雄详情链接
     */
    @TableField("hero_detail_url")
    private String heroDetailUrl;

    /**
     * 英雄头像链接
     */
    @TableField("hero_profile_url")
    private String heroProfileUrl;

    /**
     * 英雄id
     */
    @TableField("hero_id")
    private String heroId;

    /**
     * 是否爬取
     */
    @TableField("is_crawl")
    private Boolean isCrawl;

    /**
     * 类别
     */
    @TableField("category")
    private String category;

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
