package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.TalkCommentDTO;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.vo.TalkCommentVO;

/**
 * @description: 动态评论
 * @author: Polaris
 * @create: 2023-12-24 15:40
 **/
public interface ITalkCommentService extends IService<TalkComment> {
    /**
     * 新增动态评论
     *
     * @param talkCommentDTO 动态评论入参
     * @return 动态评论
     */
    TalkCommentVO addTalkComment(TalkCommentDTO talkCommentDTO);

    /**
     * 删除动态评论
     *
     * @param commentId 评论id
     */
    void deleteTalkComment(Long commentId);
}
