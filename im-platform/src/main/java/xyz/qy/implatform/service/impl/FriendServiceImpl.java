package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMPrivateMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.FriendAddDTO;
import xyz.qy.implatform.entity.Friend;
import xyz.qy.implatform.entity.FriendRequest;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FriendRequestStatusEnum;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.FriendMapper;
import xyz.qy.implatform.mapper.FriendRequestMapper;
import xyz.qy.implatform.mapper.PrivateMessageMapper;
import xyz.qy.implatform.mapper.UserMapper;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.FriendVO;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@CacheConfig(cacheNames = RedisKey.IM_CACHE_FRIEND)
@RequiredArgsConstructor
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {
    private final UserMapper userMapper;

    private final FriendRequestMapper friendRequestMapper;

    private final PrivateMessageMapper privateMessageMapper;

    private final IMClient imClient;

    @Override
    public List<FriendVO> findFriends() {
        List<Friend> friends = this.findAllFriends();
        return friends.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<Friend> findAllFriends() {
        Long userId = SessionContext.getSession().getUserId();
        LambdaQueryWrapper<Friend> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Friend::getUserId, userId);
        return this.list(wrapper);
    }

    /**
     * 查询用户的所有好友
     *
     * @param userId 用户id
     * @return 好友列表
     */
    @Override
    public List<Friend> findFriendByUserId(Long userId) {
        LambdaQueryWrapper<Friend> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Friend::getUserId, userId);
        return this.list(queryWrapper);
    }

    /**
     * 获取用户的所有好友id
     *
     * @param userId 用户id
     * @return 好友id
     */
    @Override
    public List<Long> getFriendIdsByUserId(Long userId) {
        return this.findFriendByUserId(userId).stream().map(Friend::getFriendId).collect(Collectors.toList());
    }

    /**
     * 添加好友，互相建立好友关系
     *
     * @param dto 好友信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer addFriend(FriendAddDTO dto) {
        long userId = SessionContext.getSession().getUserId();
        if (dto.getFriendId().equals(userId)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "不允许添加自己为好友");
        }
        // 查询用户好友数量
        long count = this.lambdaQuery()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getDeleted, false)
                .count();
        if (count >= 100) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "好友数量已达上限");
        }
        User friend = userMapper.selectById(dto.getFriendId());
        if (ObjectUtil.isNull(friend)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "用户不存在");
        }

        User user = userMapper.selectById(userId);

        // 判断用户是否开启了好友添加验证
        if (friend.getFriendReview()) {
            // 查询是否已存在好友申请
            LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FriendRequest::getSendId, userId);
            wrapper.eq(FriendRequest::getRecvId, dto.getFriendId());
            wrapper.eq(FriendRequest::getStatus, FriendRequestStatusEnum.APPLYING.getCode());
            int count2 = friendRequestMapper.selectCount(wrapper);
            if (count2 > 0) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "已发送好友申请");
            }

            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSendId(userId);
            friendRequest.setSendNickName(user.getNickName());
            friendRequest.setSendHeadImage(user.getHeadImage());
            friendRequest.setRecvId(dto.getFriendId());
            friendRequest.setRecvNickName(friend.getNickName());
            friendRequest.setRecvHeadImage(friend.getHeadImage());
            friendRequest.setStatus(FriendRequestStatusEnum.APPLYING.getCode());
            friendRequest.setApplyTime(LocalDateTime.now());
            friendRequest.setRemark(dto.getRemark());
            friendRequestMapper.insert(friendRequest);
            sendFriendRequestMessage(friendRequest, MessageType.FRIEND_REQUEST_ADD.code());
            return FriendRequestStatusEnum.APPLYING.getCode();
        } else {
            // 互相绑定好友关系
            FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
            proxy.bindFriend(userId, dto.getFriendId());
            proxy.bindFriend(dto.getFriendId(), userId);
            sendAddTipMessage(dto.getFriendId());
            log.info("添加好友，用户id:{},好友id:{}", userId, dto.getFriendId());
            return FriendRequestStatusEnum.AGREED.getCode();
        }
    }

    /**
     * 删除好友，双方都会解除好友关系
     *
     * @param friendId 好友的用户id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delFriend(Long friendId) {
        long userId = SessionContext.getSession().getUserId();
        // 互相解除好友关系，走代理清理缓存
        FriendServiceImpl proxy = (FriendServiceImpl) AopContext.currentProxy();
        proxy.unbindFriend(userId, friendId);
        proxy.unbindFriend(friendId, userId);
        log.info("删除好友，用户id:{},好友id:{}", userId, friendId);
    }

    /**
     * 判断用户2是否用户1的好友
     *
     * @param userId1 用户1的id
     * @param userId2 用户2的id
     */
    @Cacheable(key = "#userId1+':'+#userId2")
    @Override
    public Boolean isFriend(Long userId1, Long userId2) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId1)
                .eq(Friend::getFriendId, userId2)
                .eq(Friend::getDeleted, false);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 更新好友信息，主要是头像和昵称
     *
     * @param vo 好友vo
     */
    @Override
    public void update(FriendVO vo) {
        long userId = SessionContext.getSession().getUserId();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, vo.getId());

        Friend f = this.getOne(queryWrapper);
        if (f == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
        }

        f.setFriendNickName(vo.getNickName());
        f.setFriendRemark(vo.getFriendRemark());
        f.setFriendHeadImage(vo.getHeadImage());
        this.updateById(f);
        if (StringUtils.isNotBlank(vo.getMyHeadImageToFriend())) {
            updateMyInfoToFriend(vo);
        }
    }

    /**
     * 更新好友关于我的信息，主要是头像
     *
     * @param vo 好友vo
     */
    @Override
    public void updateMyInfoToFriend(FriendVO vo) {
        long userId = SessionContext.getSession().getUserId();
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Friend::getUserId, vo.getId())
                .eq(Friend::getFriendId, userId);
        Friend f = this.getOne(queryWrapper);
        if (f == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
        }
        f.setFriendHeadImage(vo.getMyHeadImageToFriend());
        this.updateById(f);
    }

    /**
     * 单向绑定好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    @Override
    public void bindFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (Objects.isNull(friend)) {
            friend = new Friend();
        }
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        User friendInfo = userMapper.selectById(friendId);
        friend.setFriendHeadImage(friendInfo.getHeadImageThumb());
        friend.setFriendNickName(friendInfo.getNickName());
        friend.setCreatedTime(new Date());
        friend.setDeleted(false);
        this.saveOrUpdate(friend);
        // 推送好友变化信息s
        sendAddFriendMessage(userId, friendId, friend);
    }

    /**
     * 单向解除好友关系
     *
     * @param userId   用户id
     * @param friendId 好友的用户id
     */
    @CacheEvict(key = "#userId+':'+#friendId")
    public void unbindFriend(Long userId, Long friendId) {
        // 逻辑删除
        LambdaUpdateWrapper<Friend> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(Friend::getUserId, userId);
        wrapper.eq(Friend::getFriendId, friendId);
        wrapper.set(Friend::getDeleted,true);
        this.update(wrapper);
        // 推送好友变化信息
        sendDelFriendMessage(userId, friendId);
    }

    /**
     * 查询指定的某个好友信息
     *
     * @param friendId 好友的用户id
     * @return 好友信息
     */
    @Override
    public FriendVO findFriend(Long friendId) {
        UserSession session = SessionContext.getSession();
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, session.getUserId())
                .eq(Friend::getFriendId, friendId);
        Friend friend = this.getOne(wrapper);
        if (friend == null) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "对方不是您的好友");
        }
        return convert(friend);
    }

    @Override
    public Friend findFriendInfo(Long userId, Long friendId) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        return this.getOne(wrapper);
    }

    private FriendVO convert(Friend f) {
        FriendVO vo = new FriendVO();
        vo.setId(f.getFriendId());
        vo.setHeadImage(f.getFriendHeadImage());
        vo.setNickName(f.getFriendNickName());
        vo.setDeleted(f.getDeleted());
        return vo;
    }

    @Override
    public void sendAddFriendMessage(Long userId, Long friendId, Friend friend) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_NEW.code());
        FriendVO vo = convert(friend);
        msgInfo.setContent(JSON.toJSONString(vo));
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.WEB.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void sendDelFriendMessage(Long userId, Long friendId) {
        // 推送好友状态信息
        PrivateMessageVO msgInfo = new PrivateMessageVO();
        msgInfo.setSendId(friendId);
        msgInfo.setRecvId(userId);
        msgInfo.setSendTime(new Date());
        msgInfo.setType(MessageType.FRIEND_DEL.code());
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.WEB.code()));
        sendMessage.setRecvId(userId);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void sendAddTipMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(session.getUserId());
        msg.setRecvId(friendId);
        msg.setContent("你们已成为好友，现在可以开始聊天了");
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setType(MessageType.TIP_TEXT.code());
        privateMessageMapper.insert(msg);
        // 推给对方
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvId(friendId);
        sendMessage.setSendToSelf(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
        // 推给自己
        sendMessage.setRecvId(session.getUserId());
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void sendDelTipMessage(Long friendId) {
        UserSession session = SessionContext.getSession();
        // 推送好友状态信息
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(session.getUserId());
        msg.setRecvId(friendId);
        msg.setSendTime(new Date());
        msg.setType(MessageType.TIP_TEXT.code());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setContent("你们的好友关系已被解除");
        privateMessageMapper.insert(msg);
        // 推送
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendId, IMTerminalType.WEB.code()));
        sendMessage.setRecvId(friendId);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);
    }

    @Override
    public void sendFriendRequestMessage(FriendRequest friendRequest, Integer type) {
        PrivateMessage msg = new PrivateMessage();
        msg.setSendId(friendRequest.getSendId());
        msg.setRecvId(friendRequest.getRecvId());
        msg.setContent(JSON.toJSONString(friendRequest));
        msg.setSendTime(new Date());
        msg.setStatus(MessageStatus.UNSEND.code());
        msg.setType(type);

        // 推给对方
        PrivateMessageVO messageInfo = BeanUtils.copyProperties(msg, PrivateMessageVO.class);
        IMPrivateMessage<PrivateMessageVO> sendMessage = new IMPrivateMessage<>();
        sendMessage.setSender(new IMUserInfo(friendRequest.getSendId(), IMTerminalType.WEB.code()));
        sendMessage.setRecvId(friendRequest.getRecvId());
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        sendMessage.setData(messageInfo);
        imClient.sendPrivateMessage(sendMessage);

        // 推给自己
        sendMessage.setRecvId(friendRequest.getSendId());
        imClient.sendPrivateMessage(sendMessage);
    }
}
