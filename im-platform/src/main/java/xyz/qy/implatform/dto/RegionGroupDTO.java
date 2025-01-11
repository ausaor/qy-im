package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * RegionGroupDTO
 *
 * @author Polaris
 * @since 2024-10-27
 */
@Data
public class RegionGroupDTO {
    /**
     * 地区群聊id
     */
    private Long id;

    /**
     * 地区编码
     */
    @NotBlank(message = "地区编码不能为空")
    private String code;

    /**
     * 地区群聊编号
     */
    private String num;

    /**
     * 加入地区群聊方式：0-临时，1-常驻
     */
    @Max(value = 1, message = "加入方式只能是临时或常驻")
    @Min(value = 0, message = "加入方式只能是临时或常驻")
    @NotNull(message = "加入方式不能为空")
    private Integer joinType;

    /**
     * 用户昵称
     */
    private String nickname;
}
