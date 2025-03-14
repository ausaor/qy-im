package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CharacterAvatarVO {
    private Long id;

    /**
     * 模板人物id
     */
    private Long templateCharacterId;

    /**
     * 模板人物名称
     */
    private String templateCharacterName;

    /**
     * 头像名称
     */
    private String name;

    /**
     * 头像链接
     */
    private String avatar;

    /**
     * 头像等级
     */
    private Integer level;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
