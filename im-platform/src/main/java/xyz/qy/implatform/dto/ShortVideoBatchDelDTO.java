package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ShortVideoBatchDelDTO {
    @NotEmpty(message = "id列表不能为空")
    @Size(max = 100, message = "单次最多删除100条")
    private List<Long> ids;
}
