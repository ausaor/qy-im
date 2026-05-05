package xyz.qy.implatform.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import xyz.qy.implatform.enums.ComplaintTargetTypeEnum;
import xyz.qy.implatform.enums.ValidEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ComplaintRecordDTO {
    @NotNull
    private Long targetId;

    /**
     * 投诉对象名称
     */
    @Length(max = 100)
    @NotBlank
    private String targetName;

    /**
     * 投诉对象类型（user：用户；group：群聊；regionGroup：地区群聊）
     */
    @ValidEnum(enumClass = ComplaintTargetTypeEnum.class, property = "code", message = "无效的投诉对象类型")
    @NotBlank
    private String targetType;

    /**
     * 投诉原因
     */
    @Length(max = 50)
    @NotBlank
    private String reason;

    /**
     * 图片证据
     */
    @Size(max = 9)
    private List<String> evidenceImgList;

    /**
     * 投诉内容
     */
    @Length(max = 500)
    @NotBlank
    private String content;
}
