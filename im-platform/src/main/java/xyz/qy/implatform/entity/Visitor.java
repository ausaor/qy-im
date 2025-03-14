package xyz.qy.implatform.entity;

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
 * @description: 访客信息
 * @author: Polaris
 * @create: 2023-04-29 09:27
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("im_visitor")
public class Visitor extends Model<Visitor> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 国家
     */
    @TableField("nation")
    private String nation;

    /**
     * 省
     */
    @TableField("pro")
    private String pro;

    /**
     * 市
     */
    @TableField("city")
    private String city;

    /**
     * IP地址信息
     */
    @TableField("addr")
    private String addr;

    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("operating_system")
    private String operatingSystem;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 位置新增JSON结果
     */
    @TableField("location_info")
    private String locationInfo;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
