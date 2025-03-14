package xyz.qy.implatform.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-25 09:01
 **/
@Data
public class SelectableTemplateCharacterVO {
    /**
     * 群聊id
     */
    @NotNull(message = "群聊id不能为空")
    private Long groupId;

    /**
     * 模板群聊id
     */
    @NotNull(message = "模板群聊id不能为空")
    private Long templateGroupId;
}
