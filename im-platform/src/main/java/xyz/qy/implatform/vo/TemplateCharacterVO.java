package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 16:36
 **/
@Data
public class TemplateCharacterVO {
    private Long id;

    /** 模板群聊id */
    private Long templateGroupId;

    /** 人物名称 */
    private String name;

    /** 人物描述 */
    private String description;

    /** 人物头像 */
    private String avatar;


    /** 是否删除：0-否；1-是 */
    private Boolean deleted;

    /** 状态 */
    private String status;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 是否可选择
     * true：可选
     * false：不可选
     */
    private Boolean selectable = Boolean.TRUE;

    /**
     * 用于标识可选择的模板人物是否已被选择
     */
    private Boolean choosed = Boolean.FALSE;

    /**
     * 是否创建人
     */
    private Boolean isOwner = false;

    /**
     * 模板人物头像
     */
    private List<CharacterAvatarVO> characterAvatarVOList;
}
