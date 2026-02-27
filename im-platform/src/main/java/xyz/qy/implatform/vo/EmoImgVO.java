package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class EmoImgVO {
    private Integer id;

    private Integer albumId;

    private String name;

    private String imageUrl;

    private String thumbUrl;

    private Integer width;

    private Integer height;
}
