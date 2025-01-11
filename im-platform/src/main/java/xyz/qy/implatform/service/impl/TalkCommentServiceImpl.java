package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.dto.TalkCommentDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkCommentMapper;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.TalkCommentVO;

import java.util.Objects;

/**
 * @description: 动态评论
 * @author: Polaris
 * @create: 2023-12-24 15:42
 **/
@Slf4j
@Service
public class TalkCommentServiceImpl extends ServiceImpl<TalkCommentMapper, TalkComment> implements ITalkCommentService {
    @Autowired
    private ITalkService talkService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Transactional
    @Lock(prefix = "im:talk:comment", key = "#talkCommentDTO.getTalkId()")
    @Override
    public TalkCommentVO addTalkComment(TalkCommentDTO talkCommentDTO) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        User user = userService.getById(myUserId);

        Long talkId = talkCommentDTO.getTalkId();
        Talk talk = talkService.getById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        if (!Objects.isNull(talkCommentDTO.getCharacterId())) {
            if (talkService.verifyTalkCommentCharacter(talkId, talkCommentDTO.getCharacterId())) {
                throw new GlobalException("只能使用选择过的角色");
            }
        }

        TalkComment talkComment = new TalkComment();
        talkComment.setTalkId(talkId);
        talkComment.setUserId(myUserId);
        talkComment.setCreateBy(myUserId);
        talkComment.setContent(SensitiveUtil.filter(talkCommentDTO.getContent()));
        talkComment.setIp(user.getIpAddress());
        talkComment.setIpAddress(user.getProvince());
        if (ObjectUtil.isNotNull(talkCommentDTO.getCharacterId())) {
            TemplateCharacter templateCharacter = templateCharacterService.getById(talkCommentDTO.getCharacterId());
            if (Objects.isNull(templateCharacter)) {
                throw new GlobalException("当前角色不存在");
            }
            talkComment.setCharacterId(templateCharacter.getId());
            talkComment.setUserNickname(talkCommentDTO.getUserNickname());
            talkComment.setUserAvatar(talkCommentDTO.getUserAvatar());
        } else {
            talkComment.setUserAvatar(user.getHeadImage());
            talkComment.setUserNickname(user.getNickName());
        }

        TalkComment replyTalkComment = null;
        if (!Objects.isNull(talkCommentDTO.getReplyCommentId())) {
            replyTalkComment = baseMapper.selectById(talkCommentDTO.getReplyCommentId());
            if (!Objects.isNull(replyTalkComment)) {
                if (myUserId.equals(replyTalkComment.getUserId())) {
                    throw new GlobalException("不能回复自己的评论");
                }
                talkComment.setReplyCommentId(talkCommentDTO.getReplyCommentId());
                talkComment.setReplyUserId(replyTalkComment.getUserId());
                talkComment.setReplyUserNickname(replyTalkComment.getUserNickname());
                talkComment.setReplyUserAvatar(replyTalkComment.getUserAvatar());
            }
        }

        this.save(talkComment);
        TalkCommentVO talkCommentVO = BeanUtils.copyProperties(talkComment, TalkCommentVO.class);

        talkCommentVO.setIsOwner(Boolean.TRUE);
        return talkCommentVO;
    }

    @Override
    public void deleteTalkComment(Long commentId) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        TalkComment talkComment = this.getById(commentId);
        if (Objects.isNull(talkComment)) {
            throw new GlobalException("评论不存在");
        }
        if (!myUserId.equals(talkComment.getUserId())) {
            throw new GlobalException("您不是当前评论的作者");
        }
        talkComment.setUpdateBy(myUserId);
        talkComment.setDeleted(Boolean.TRUE);
        boolean update = this.updateById(talkComment);
        if (update) {
            log.info("删除动态评论成功，id:{}", talkComment.getId());
        } else {
            log.error("删除动态评论失败，id:{}", talkComment.getId());
            throw new GlobalException("删除动态评论失败");
        }
    }
}
