package xyz.qy.implatform.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 地区群聊信息VO
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@ApiModel("地区群聊VO")
public class RegionGroupVO {
    /**
     * 群id
     */
    private Long id;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 地区群聊名称
     */
    private String regionGroupName;

    /**
     * 地区群聊编号
     */
    private String num;

    /**
     * 地区群聊群主id
     */
    private Long ownerId;

    /**
     * 群主用户名
     */
    private String groupAdmin;

    /**
     * 群主生效时间
     */
    private Date effectiveTime;

    /**
     * 群聊失效时间
     */
    private Date expirationTime;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 备注
     */
    private String remark;

    /**
     * 群聊是否开启了全员禁言
     */
    private Boolean isBanned;

    /**
     * 被禁止发言类型（sys：系统禁止；master：群主禁止）
     */
    private String banType;

    /**
     * 禁止发言失效时间
     */
    private Date banExpireTime;

    /**
     * 加入地区群聊方式：0-临时，1-常驻
     */
    private Integer joinType;

    /**
     * 用户昵称
     */
    private String aliasName;

    /**
     * 是否退出
     */
    private Boolean quit;
}
