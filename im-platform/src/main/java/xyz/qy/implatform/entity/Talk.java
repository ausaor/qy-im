package xyz.qy.implatform.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 动态
 * @author: Polaris
 * @create: 2023-11-19 21:29
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_talk")
public class Talk extends BaseEntity{
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色人物id
     */
    @TableField(value = "character_id", updateStrategy = FieldStrategy.IGNORED)
    private Long characterId;

    /**
     * 头像id
     */
    @TableField(value = "avatar_id", updateStrategy = FieldStrategy.IGNORED)
    private Long avatarId;

    /**
     * 用户昵称
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 文件列表
     */
    @TableField("files")
    private String files;

    /**
     * 公布范围
     */
    @TableField("scope")
    private Integer scope;

    /**
     * 发布地址
     */
    @TableField("address")
    private String address;

    /**
     * 分类（private：个人，group：群聊，region：地区）
     */
    @TableField("category")
    private String category;

    /**
     * 群聊空间可见
     */
    @TableField("group_visible")
    private Boolean groupVisible;

    /**
     * 地区空间可见
     */
    @TableField("region_visible")
    private Boolean regionVisible;

    /**
     * 角色空间可见
     */
    @TableField("character_visible")
    private Boolean characterVisible;

    /**
     * 群聊id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 地区编码
     */
    @TableField("region_code")
    private String regionCode;

    @TableField("group_template_id")
    private Long groupTemplateId;
}
