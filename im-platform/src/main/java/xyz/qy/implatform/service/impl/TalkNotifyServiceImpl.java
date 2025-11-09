package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.dto.TalkNotifyQueryDTO;
import xyz.qy.implatform.dto.TalkNotifyUpdateDTO;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.enums.TalkCategoryEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkNotifyMapper;
import xyz.qy.implatform.service.ITalkCommentService;
import xyz.qy.implatform.service.ITalkNotifyService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.TalkCommentVO;
import xyz.qy.implatform.vo.TalkNotifyVO;
import xyz.qy.implatform.vo.TalkStarVO;
import xyz.qy.implatform.vo.TalkVO;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TalkNotifyServiceImpl extends ServiceImpl<TalkNotifyMapper, TalkNotify> implements ITalkNotifyService {
    @Resource
    private ITalkService talkService;

    @Resource
    private ITalkCommentService talkCommentService;

    @Resource
    private ITalkStarService  talkStarService;

    @Override
    public void readedTalkNotify(TalkNotifyUpdateDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (TalkCategoryEnum.GROUP.getCode().equals(dto.getCategory()) && ObjectUtil.isNull(dto.getGroupId())) {
            throw new GlobalException("群聊Id不能为空");
        } else if (TalkCategoryEnum.REGION.getCode().equals(dto.getCategory()) && StringUtils.isBlank(dto.getRegionCode())) {
            throw new GlobalException("地区编码不能为空");
        }

        LambdaUpdateWrapper<TalkNotify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TalkNotify::getUserId, userId);
        updateWrapper.eq(TalkNotify::getCategory, dto.getCategory());
        updateWrapper.eq(TalkNotify::getIsRead, false);
        updateWrapper.eq(ObjectUtil.isNotNull(dto.getGroupId()), TalkNotify::getGroupId, dto.getGroupId());
        updateWrapper.eq(StringUtils.isNotBlank(dto.getRegionCode()), TalkNotify::getRegionCode, dto.getRegionCode());
        updateWrapper.set(TalkNotify::getIsRead, true);
        updateWrapper.set(TalkNotify::getUpdateTime, LocalDateTime.now());

        this.update(updateWrapper);
    }

    @Override
    public PageResultVO pageQueryTalkNotify(TalkNotifyQueryDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 查询用户的动态通知消息
        LambdaQueryWrapper<TalkNotify> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalkNotify::getUserId, userId);
        wrapper.eq(TalkNotify::getCategory, dto.getCategory());
        wrapper.eq(ObjectUtil.isNotNull(dto.getGroupId()), TalkNotify::getGroupId, dto.getGroupId());
        wrapper.eq(StringUtils.isNotBlank(dto.getRegionCode()), TalkNotify::getRegionCode, dto.getRegionCode());
        wrapper.orderByDesc(TalkNotify::getCreateTime);
        Page<TalkNotify> selectPage = this.baseMapper.selectPage(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), wrapper);

        if (CollectionUtils.isNotEmpty(selectPage.getRecords())) {
            List<TalkNotify> records = selectPage.getRecords();
            Set<Long> talkIds = records.stream().map(TalkNotify::getTalkId).collect(Collectors.toSet());
            Set<Long> commentIds = records.stream().map(TalkNotify::getCommentId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Long> starIds = records.stream().map(TalkNotify::getStarId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());

            List<Talk> talks = talkService.listByIds(talkIds);

            Map<Long, Talk> talkMap = talks.stream().collect(Collectors.toMap(Talk::getId, Function.identity()));
            Map<Long, TalkComment> talkCommentMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(commentIds)) {
                List<TalkComment> talkComments = talkCommentService.listByIds(commentIds);
                talkCommentMap = talkComments.stream().collect(Collectors.toMap(TalkComment::getId, Function.identity()));
            }

            Map<Long, TalkStar> talkStarMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(starIds)) {
                List<TalkStar> talkStars = talkStarService.listByIds(starIds);
                talkStarMap = talkStars.stream().collect(Collectors.toMap(TalkStar::getId, Function.identity()));
            }
            List<TalkNotifyVO> talkNotifyVOS = BeanUtils.copyPropertiesList(records, TalkNotifyVO.class);
            List<TalkNotifyVO> result = new ArrayList<>();
            for (TalkNotifyVO talkNotifyVO : talkNotifyVOS) {
                if (ObjectUtil.isNotNull(talkNotifyVO.getTalkId())) {
                    Talk talk = talkMap.get(talkNotifyVO.getTalkId());
                    if (talk.getDeleted()) {
                        continue;
                    }
                    TalkVO talkVO = BeanUtils.copyProperties(talk, TalkVO.class);
                    if (StringUtils.isNotBlank(talk.getFiles())) {
                        talkVO.setFileList(JSONArray.parseArray(talk.getFiles()));
                    }

                    // 当前用户是动态作者
                    if (userId.equals(talk.getUserId()) && ObjectUtil.isNotNull(talk.getCharacterId())) {
                        talkVO.setCommentCharacterName(talk.getNickName());
                        talkVO.setCommentCharacterAvatar(talk.getAvatar());
                        talkVO.setCommentCharacterId(talk.getCharacterId());
                        talkVO.setCommentCharacterAvatarId(talk.getAvatarId());
                    }
                    talkNotifyVO.setTalk(talkVO);
                }
                if (ObjectUtil.isNotNull(talkNotifyVO.getCommentId())) {
                    TalkComment talkComment = talkCommentMap.get(talkNotifyVO.getCommentId());
                    if (talkComment.getDeleted()) {
                        continue;
                    }

                    talkNotifyVO.setCommentUserId(talkComment.getUserId());
                    talkNotifyVO.setAvatar(talkComment.getUserAvatar());
                    talkNotifyVO.setNickname(talkComment.getUserNickname());
                    talkNotifyVO.setTalkComment(BeanUtils.copyProperties(talkComment, TalkCommentVO.class));

                    // 查询当前评论的回复
                    if (ObjectUtil.isNotNull(talkComment.getReplyCommentId())) {
                        TalkComment replytalkComment = talkCommentService.getById(talkComment.getReplyCommentId());
                        if (!replytalkComment.getDeleted()) {
                            List<TalkCommentVO> replyTalkCommentList = new ArrayList<>();
                            TalkCommentVO talkCommentVO = BeanUtils.copyProperties(replytalkComment, TalkCommentVO.class);
                            replyTalkCommentList.add(talkCommentVO);
                            talkNotifyVO.setReplyTalkComment(replyTalkCommentList);
                            if (replytalkComment.getUserId().equals(userId)) {
                                talkNotifyVO.getTalk().setCommentCharacterId(replytalkComment.getCharacterId());
                                talkNotifyVO.getTalk().setCommentCharacterName(replytalkComment.getUserNickname());
                                talkNotifyVO.getTalk().setCommentCharacterAvatar(replytalkComment.getUserAvatar());
                                talkNotifyVO.getTalk().setCommentCharacterAvatarId(replytalkComment.getAvatarId());
                            }
                        }
                    }
                    // 查询当前用户对当前评论的所有评论
                    List<TalkComment> talkCommentList = talkCommentService.lambdaQuery().eq(TalkComment::getReplyCommentId, talkComment.getId())
                            .eq(TalkComment::getUserId, userId)
                            .eq(TalkComment::getDeleted, Boolean.FALSE).list();
                    if (CollectionUtils.isNotEmpty(talkCommentList)) {
                        List<TalkCommentVO> talkCommentVOS = BeanUtils.copyPropertiesList(talkCommentList, TalkCommentVO.class);
                        if (CollectionUtils.isNotEmpty(talkNotifyVO.getReplyTalkComment())) {
                            talkNotifyVO.getReplyTalkComment().addAll(talkCommentVOS);
                        } else {
                            talkNotifyVO.setReplyTalkComment(talkCommentVOS);
                        }

                        // 找到当前用户评论，并且角色id不为空的数据
                        Optional<TalkComment> talkCommentOptional = talkCommentList.stream().filter(item -> item.getUserId().equals(userId)
                                && ObjectUtil.isNotNull(item.getCharacterId())).findFirst();
                        talkCommentOptional.ifPresent(talkComment1 -> {
                            talkNotifyVO.getTalk().setCommentCharacterAvatarId(talkComment1.getAvatarId());
                            talkNotifyVO.getTalk().setCommentCharacterId(talkComment1.getCharacterId());
                            talkNotifyVO.getTalk().setCommentCharacterName(talkComment1.getUserNickname());
                            talkNotifyVO.getTalk().setCommentCharacterAvatar(talkComment1.getUserAvatar());
                        });
                    }
                } else if (ObjectUtil.isNotNull(talkNotifyVO.getStarId())) {
                    TalkStar talkStar = talkStarMap.get(talkNotifyVO.getStarId());
                    if (talkStar.getDeleted()) {
                        continue;
                    }
                    talkNotifyVO.setCommentUserId(talkStar.getUserId());
                    talkNotifyVO.setTalkStar(BeanUtils.copyProperties(talkStar, TalkStarVO.class));
                    talkNotifyVO.setAvatar(talkStar.getAvatar());
                    talkNotifyVO.setNickname(talkStar.getNickname());
                }

                result.add(talkNotifyVO);
            }

            return PageResultVO.builder().data(result).total(selectPage.getTotal()).build();
        }

        return PageResultVO.builder().data(Collections.emptyList()).total(selectPage.getTotal()).build();
    }
}
