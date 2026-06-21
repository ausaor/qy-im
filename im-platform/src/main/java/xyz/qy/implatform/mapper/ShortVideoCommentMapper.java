package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import xyz.qy.implatform.entity.ShortVideoComment;

import java.util.List;
import java.util.Map;

public interface ShortVideoCommentMapper extends BaseMapper<ShortVideoComment> {

    /**
     * 统计每个顶级评论的子评论数
     *
     * @param topCommentIds 顶级评论ID列表
     * @return key: top_reply_comment_id, value: count
     */
    List<Map<String, Object>> countChildByTopCommentIds(@Param("topCommentIds") List<Long> topCommentIds);
}
