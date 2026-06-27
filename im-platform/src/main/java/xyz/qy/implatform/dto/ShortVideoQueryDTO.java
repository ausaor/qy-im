package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.enums.SectionEnum;
import xyz.qy.implatform.enums.ValidEnum;

@Data
public class ShortVideoQueryDTO {
    private Long id;

    private Long userId;

    private Long objectId;

    private String type;

    private Integer scope;

    private String title;

    private Integer status;

    @ValidEnum(enumClass = SectionEnum.class, property = "code", message = "无效的查询参数")
    private String section;
}
