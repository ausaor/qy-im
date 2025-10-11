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
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 用户
 *
 * @author Polaris
 * @since 2022-10-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_user")
public class User extends Model<User> {
    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 用户名
     */
    @TableField("nick_name")
    private String nickName;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 角色
     */
    @TableField("role")
    private String role;

    /**
     * 性别
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 用户类型  1:普通用户 2:审核专用账户
     */
    @TableField("type")
    private Integer type;

    /**
     * 头像
     */
    @TableField("head_image")
    private String headImage;

    /**
     * 头像缩略图
     */
    @TableField("head_image_thumb")
    private String headImageThumb;


    /**
     * 个性签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * QQ开放ID
     */
    @TableField("qq_open_id")
    private String qqOpenId;

    /**
     * QQ访问令牌
     */
    @TableField("qq_access_token")
    private String qqAccessToken;

    /**
     * 登录类型
     */
    @TableField("login_type")
    private Integer loginType;

    /**
     * 是否禁止登录
     */
    @TableField("is_disable")
    private Boolean isDisable;

    /**
     * 用户登录IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * ip来源
     */
    @TableField("ip_source")
    private String ipSource;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 是否被禁止发言
     */
    @TableField("is_banned")
    private Boolean isBanned;

    /**
     * 语音自动播放
     */
    @TableField("auto_play")
    private Boolean autoPlay;

    /**
     * 消息提示音
     */
    @TableField("sound_play")
    private Boolean soundPlay;

    /**
     * 添加好友是否需要审核
     */
    @TableField("friend_review")
    private Boolean friendReview;

    /**
     * 邀请进群组是否需要审核
     */
    @TableField("group_review")
    private Boolean groupReview;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
