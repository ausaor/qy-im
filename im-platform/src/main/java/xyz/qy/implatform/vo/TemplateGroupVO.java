package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 11:16
 **/
@Data
public class TemplateGroupVO {
    private Long id;

    /** 群名称 */
    private String groupName;

    /** 描述 */
    private String description;

    /** 群头像 */
    private String avatar;

    /** 群人数 */
    private Integer count;

    /** 状态 */
    private String status;

    /** 是否删除：0-否；1-是 */
    private Boolean deleted;

    /** 创建人id */
    private String createBy;

    /**
     * 创建人
     */
    private String creator;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Date updateTime;

    /**
     * 是否模板群聊创建人
     */
    private Boolean isOwner = false;

    /**
     * 模板角色
     */
    private List<TemplateCharacterVO> templateCharacterVOList;

    /**
     * 角色ids
     */
    private List<Long> characterIds;
}
