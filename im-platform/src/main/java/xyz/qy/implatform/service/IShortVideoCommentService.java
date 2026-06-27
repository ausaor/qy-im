package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.ShortVideoCommentAddDTO;
import xyz.qy.implatform.dto.ShortVideoCommentDelDTO;
import xyz.qy.implatform.dto.ShortVideoCommentQueryDTO;
import xyz.qy.implatform.entity.ShortVideoComment;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.ShortVideoCommentVO;

import java.util.List;

public interface IShortVideoCommentService extends IService<ShortVideoComment> {
    List<ShortVideoCommentVO> listShortVideoComments(ShortVideoCommentQueryDTO dto);

    PageResultVO<List<ShortVideoCommentVO>> pageShortVideoComments(ShortVideoCommentQueryDTO dto);

    ShortVideoCommentVO addShortVideoComment(ShortVideoCommentAddDTO dto);

    void deleteShortVideoComment(ShortVideoCommentDelDTO dto);

    Integer getCommentCountByVideoId(Long videoId);

    void addCommentLike(Long commentId);
}
