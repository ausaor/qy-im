package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CharacterEmoSaveDTO {
    @NotNull(message="群聊模板id不可为空")
    private Long templateGroupId;

    private Long characterId;

    @NotNull(message = "台词不能为空")
    private List<CharacterEmoDTO> emos;
}
