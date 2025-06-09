package xyz.qy.implatform.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("群聊通知更新DTO")
public class TalkNotifyUpdateDTO {
    @NotBlank
    private String category;

    private Long groupId;

    private String regionCode;
}
