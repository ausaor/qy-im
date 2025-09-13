package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GroupRequestUpdateDTO {
    @NotNull
    private Long id;

    @NotNull
    private Long templateCharacterId;
}
