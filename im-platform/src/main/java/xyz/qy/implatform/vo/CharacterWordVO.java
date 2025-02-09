package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CharacterWordVO {
    private Long id;

    private Long templateGroupId;

    private Long characterId;

    private String characterName;

    private String word;

    private String voice;

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
    private Long createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Boolean isOwner = false;
}
