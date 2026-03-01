package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class EmoFavoriteVO {
    private String id;

    private Integer emoId;

    private Integer albumId;

    private String name;

    private String imageUrl;

    private String thumbUrl;

    private Integer width;

    private Integer height;
}
