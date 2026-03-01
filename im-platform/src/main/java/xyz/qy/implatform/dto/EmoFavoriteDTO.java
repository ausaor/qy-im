package xyz.qy.implatform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EmoFavoriteDTO {
    private Integer emoId;

    private Integer albumId;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;
}
