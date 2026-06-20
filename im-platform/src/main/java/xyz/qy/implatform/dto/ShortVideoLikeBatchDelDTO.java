package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ShortVideoLikeBatchDelDTO {
    @NotEmpty(message = "ids不能为空")
    private List<Long> ids;
}
