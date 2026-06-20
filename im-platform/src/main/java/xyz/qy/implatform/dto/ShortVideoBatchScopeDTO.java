package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.enums.ValidEnum;
import xyz.qy.implatform.enums.ViewScopeEnum;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ShortVideoBatchScopeDTO {
    @NotEmpty(message = "id列表不能为空")
    @Size(max = 100, message = "单次最多操作100条")
    private List<Long> ids;

    @NotNull(message = "未选择公布范围")
    @ValidEnum(enumClass = ViewScopeEnum.class, property = "code", message = "无效的公布范围")
    private Integer scope;
}
