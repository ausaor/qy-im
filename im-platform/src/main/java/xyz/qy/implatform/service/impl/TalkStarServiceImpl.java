package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.imcommon.model.IMTalkMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.dto.TalkStarDTO;
import xyz.qy.implatform.dto.UserDataAuthDTO;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.Talk;
import xyz.qy.implatform.entity.TalkNotify;
import xyz.qy.implatform.entity.TalkStar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.TalkNotifyActionTypeEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TalkStarMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ITalkNotifyService;
import xyz.qy.implatform.service.ITalkService;
import xyz.qy.implatform.service.ITalkStarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.TalkMessageVO;
import xyz.qy.implatform.vo.TalkStarVO;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * @description: 动态赞星
 * @author: Polaris
 * @create: 2023-12-24 15:39
 **/
@Service
public class TalkStarServiceImpl extends ServiceImpl<TalkStarMapper, TalkStar> implements ITalkStarService {
    @Autowired
    private ITalkService talkService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Resource
    private ITalkNotifyService talkNotifyService;

    @Resource
    private IMClient imClient;

    @Transactional
    @Lock(prefix = "im:talk:comment", key = "#talkStarDTO.getTalkId()")
    @Override
    public TalkStarVO like(TalkStarDTO talkStarDTO) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();

        LambdaQueryWrapper<TalkStar> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TalkStar::getTalkId, talkStarDTO.getTalkId())
                .eq(TalkStar::getUserId, myUserId)
                .eq(TalkStar::getDeleted, false);
        TalkStar one = this.getOne(wrapper);

        if (ObjectUtil.isNotNull(one)) {
            throw new GlobalException("您已点赞过此动态");
        }

        User user = userService.getById(myUserId);

        Long talkId = talkStarDTO.getTalkId();
        Talk talk = talkService.getById(talkId);
        if (Objects.isNull(talk) || talk.getDeleted()) {
            throw new GlobalException("当前动态已被删除");
        }
        if (!Objects.isNull(talkStarDTO.getCharacterId())) {
            if (talkService.verifyTalkCommentCharacter(talkId, talkStarDTO.getCharacterId(), talkStarDTO.getAvatarId())) {
                throw new GlobalException("只能使用选择过的角色或所选角色已被使用");
            }
        }

        if (!talkService.verifyUserDataAuth(new UserDataAuthDTO(talk.getCategory(), myUserId, talk.getUserId(), talk.getGroupId(), talk.getRegionCode()))) {
            throw new GlobalException("您无权限操作");
        }
        TalkStar talkStar = new TalkStar();
        talkStar.setTalkId(talkId);
        talkStar.setUserId(myUserId);
        talkStar.setCreateBy(myUserId);
        if (ObjectUtil.isNotNull(talkStarDTO.getCharacterId())) {
            TemplateCharacter templateCharacter = templateCharacterService.getById(talkStarDTO.getCharacterId());
            if (Objects.isNull(templateCharacter)) {
                throw new GlobalException("当前角色不存在");
            }
            talkStar.setCharacterId(templateCharacter.getId());
            talkStar.setNickname(templateCharacter.getName());
            talkStar.setAvatar(templateCharacter.getAvatar());

            if (ObjectUtil.isNotNull(talkStarDTO.getAvatarId())) {
                CharacterAvatar characterAvatar = characterAvatarService.getById(talkStarDTO.getAvatarId());
                if (ObjectUtil.isNotNull(characterAvatar)) {
                    if (!characterAvatar.getTemplateCharacterId().equals(templateCharacter.getId())) {
                        throw new GlobalException("所选角色头像不属于当前角色");
                    }
                    talkStar.setAvatarId(characterAvatar.getId());
                    talkStar.setAvatar(characterAvatar.getAvatar());
                    if (!characterAvatar.getLevel().equals(0)) {
                        talkStar.setNickname(characterAvatar.getName());
                    }
                }
            }
        } else {
            talkStar.setAvatar(user.getHeadImage());
            talkStar.setNickname(user.getNickName());
        }

        this.save(talkStar);

        if (!myUserId.equals(talk.getUserId())) {
            TalkNotify talkNotify = new TalkNotify();
            talkNotify.setTalkId(talkId);
            talkNotify.setUserId(talk.getUserId());
            talkNotify.setStarId(talkStar.getId());
            talkNotify.setCategory(talk.getCategory());
            talkNotify.setGroupId(talk.getGroupId());
            talkNotify.setRegionCode(talk.getRegionCode());
            talkNotify.setActionType(TalkNotifyActionTypeEnum.LIKE.getCode());
            talkNotify.setCreateTime(LocalDateTime.now());
            talkNotifyService.save(talkNotify);

            TalkMessageVO msgInfo = new TalkMessageVO();
            msgInfo.setType(3);
            msgInfo.setTalk(talk);
            msgInfo.setTalkStar(talkStar);

            IMTalkMessage<TalkMessageVO> sendMessage = new IMTalkMessage<>();
            sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
            sendMessage.setRecvIds(Collections.singletonList(talk.getUserId()));
            sendMessage.setSendResult(false);
            sendMessage.setData(msgInfo);
            imClient.sendTalkMessage(sendMessage);
        }
        TalkStarVO talkStarVO = BeanUtils.copyProperties(talkStar, TalkStarVO.class);
        talkStarVO.setIsOwner(Boolean.TRUE);
        return talkStarVO;
    }

    @Lock(prefix = "im:talk:comment", key = "#talkStarDTO.getTalkId()")
    @Transactional
    @Override
    public void cancelLike(TalkStarDTO talkStarDTO) {
        UserSession session = SessionContext.getSession();
        Long myUserId = session.getUserId();
        LambdaQueryWrapper<TalkStar> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TalkStar::getTalkId, talkStarDTO.getTalkId());
        wrapper.eq(TalkStar::getUserId, myUserId);
        wrapper.eq(TalkStar::getDeleted, false);

        TalkStar talkStar = baseMapper.selectOne(wrapper);

        if (ObjectUtil.isNull(talkStar)) {
            throw new GlobalException("点赞记录不存在");
        }

        baseMapper.delete(wrapper);
        
        // 删除点赞提醒记录
        LambdaUpdateWrapper<TalkNotify> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TalkNotify::getStarId, talkStar.getId());
        updateWrapper.eq(TalkNotify::getActionType, TalkNotifyActionTypeEnum.LIKE.getCode());
        updateWrapper.eq(TalkNotify::getTalkId, talkStarDTO.getTalkId());

        talkNotifyService.remove(updateWrapper);
    }
}
