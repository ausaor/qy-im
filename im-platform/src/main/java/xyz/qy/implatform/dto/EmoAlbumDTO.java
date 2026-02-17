package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EmoAlbumDTO {
    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "图标地址不能为空")
    private String logoUrl;
}
