package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class ShortVideoMessageVO {

    private Integer type;

    private ShortVideoVO shortVideo;

    private ShortVideoCommentVO shortVideoComment;

    private ShortVideoLikeVO shortVideoLike;

    private ShortVideoFavoriteVO shortVideoFavorite;
}
