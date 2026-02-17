package xyz.qy.implatform.vo;

import lombok.Data;

import java.util.List;

@Data
public class EmoAlbumVO {
    private Integer id;

    private String name;

    private String logoUrl;

    private List<EmoImgVO> emoImgList;
}
