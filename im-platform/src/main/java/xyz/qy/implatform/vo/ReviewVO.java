package xyz.qy.implatform.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description: 审核类VO
 * @author: Polaris
 * @create: 2023-08-05 20:08
 **/
@Data
public class ReviewVO {
    /**
     * 审核意见
     */
    @NotNull(message = "审核意见不能为空")
    private String comments;

    /**
     * 模板群聊id
     */
    private Long templateGroupId;

    /**
     * 模板人物id
     */
    private Long templateCharacterId;
}
