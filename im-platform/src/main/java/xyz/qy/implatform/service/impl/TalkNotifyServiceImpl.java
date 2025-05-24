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
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkComment;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.entity.TalkStar;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public void readedTalkNotify(String category) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaUpdateWrapper<TalkNotify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TalkNotify::getUserId, userId);
        updateWrapper.eq(TalkNotify::getCategory, category);
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
            for (TalkNotifyVO talkNotifyVO : talkNotifyVOS) {
                if (ObjectUtil.isNotNull(talkNotifyVO.getTalkId())) {
                    Talk talk = talkMap.get(talkNotifyVO.getTalkId());
                    TalkVO talkVO = BeanUtils.copyProperties(talk, TalkVO.class);
                    if (StringUtils.isNotBlank(talk.getFiles())) {
                        talkVO.setFileList(JSONArray.parseArray(talk.getFiles()));
                    }
                    talkNotifyVO.setTalk(talkVO);
                }
                if (ObjectUtil.isNotNull(talkNotifyVO.getCommentId())) {
                    TalkComment talkComment = talkCommentMap.get(talkNotifyVO.getCommentId());
                    talkNotifyVO.setCommentUserId(talkComment.getUserId());
                    talkNotifyVO.setAvatar(talkComment.getUserAvatar());
                    talkNotifyVO.setNickname(talkComment.getUserNickname());
                    talkNotifyVO.setTalkComment(BeanUtils.copyProperties(talkComment, TalkCommentVO.class));
                    if (ObjectUtil.isNotNull(talkComment.getReplyCommentId())) {
                        TalkComment replytalkComment = talkCommentService.getById(talkComment.getReplyCommentId());
                        talkNotifyVO.setReplyTalkComment(Collections.singletonList(BeanUtils.copyProperties(replytalkComment, TalkCommentVO.class)));
                    }
                } else if (ObjectUtil.isNotNull(talkNotifyVO.getStarId())) {
                    TalkStar talkStar = talkStarMap.get(talkNotifyVO.getStarId());
                    talkNotifyVO.setCommentUserId(talkStar.getUserId());
                    talkNotifyVO.setTalkStar(BeanUtils.copyProperties(talkStar, TalkStarVO.class));
                    talkNotifyVO.setAvatar(talkStar.getAvatar());
                    talkNotifyVO.setNickname(talkStar.getNickname());
                }
            }

            return PageResultVO.builder().data(talkNotifyVOS).total(selectPage.getTotal()).build();
        }

        return PageResultVO.builder().data(Collections.emptyList()).total(selectPage.getTotal()).build();
    }
}
