package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.contant.IMConstant;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMRegionGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imcommon.util.CommaTextUtils;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.contant.RegionGroupConst;
import xyz.qy.implatform.dto.RegionGroupMessageDTO;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMember;
import xyz.qy.implatform.entity.RegionGroupMessage;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.RegionGroupMessageMapper;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupMessageService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.RegionGroupMessageVO;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 地区群聊消息
 *
 * @author Polaris
 * @since 2022-11-03
 */
@Slf4j
@Service
public class RegionGroupMessageServiceImpl extends ServiceImpl<RegionGroupMessageMapper, RegionGroupMessage> implements IRegionGroupMessageService {

    @Resource
    private IRegionGroupService regionGroupService;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IUserService userService;

    @Resource
    private IMClient imClient;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public RegionGroupMessageVO sendMessage(RegionGroupMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        RegionGroup regionGroup = regionGroupService.getById(dto.getRegionGroupId());
        if (Objects.isNull(regionGroup)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (regionGroup.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }

        // 根据加入类型判断用户是否在群里
        checkUserInRegionGroup(dto.getJoinType(), session.getUserId(), regionGroup);

        User user = userService.getById(session.getUserId());
        Date now = new Date();
        // 判断是否有群主，没有群主每位群成员24小时消息数量限制50条
        if (ObjectUtil.isNull(regionGroup.getLeaderId()) || now.after(regionGroup.getExpirationTime())) {
            Boolean exists = redisCache.hasKey(RedisKey.IM_REGION_GROUP_MSG_LIMIT + regionGroup.getId() + ":" + user.getId());
            int count = redisCache.incrementInt(RedisKey.IM_REGION_GROUP_MSG_LIMIT + regionGroup.getId() + ":" + user.getId());
            if (!exists) {
                redisCache.expire(RedisKey.IM_REGION_GROUP_MSG_LIMIT + regionGroup.getId() + ":" + user.getId(), RegionGroupConst.MSG_LIMIT_TIME, TimeUnit.HOURS);
            }
            if (count > RegionGroupConst.MSG_LIMIT_COUNT) {
                throw new GlobalException("当前群聊没有群主，您在" + RegionGroupConst.MSG_LIMIT_TIME + "小时内发言数已超过" + RegionGroupConst.MSG_LIMIT_COUNT+ "条，不能再继续发言");
            }
        }

        // 查询常驻用户id
        List<Long> userIds = regionGroupMemberService.findUserIdsByRegionGroupId(regionGroup.getId());

        // 查询临时用户id
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER + regionGroup.getCode() + ":" + regionGroup.getNum() + ":*");
        for (String key : keys) {
            Object object = redisCache.getCacheObject(key);
            if (ObjectUtil.isNotNull(object)) {
                UserSession userSession = Convert.convert(UserSession.class, object);
                userIds.add(userSession.getUserId());
            }
        }

        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        RegionGroupMessage msg = BeanUtils.copyProperties(dto, RegionGroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(now);
        msg.setSendNickName(user.getNickName());
        if (MessageType.TEXT.code().equals(msg.getType())) {
            msg.setContent(SensitiveUtil.filter(msg.getContent()));
        }
        if (CollUtil.isNotEmpty(dto.getAtUserIds())) {
            msg.setAtUserIds(StrUtil.join(",", dto.getAtUserIds()));
        }
        this.save(msg);
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 群发
        RegionGroupMessageVO msgInfo = BeanUtils.copyProperties(msg, RegionGroupMessageVO.class);
        msgInfo.setSendUserAvatar(user.getHeadImage());
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendRegionGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getRegionGroupId(), dto.getContent());
        return msgInfo;
    }

    private void checkUserInRegionGroup(Integer joinType, Long userId, RegionGroup regionGroup) {
        // 临时加入的判断地区群聊的临时用户是否有当前用户
        if (joinType == 0) {
            Object object = redisCache.getCacheObject(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER + regionGroup.getCode() + ":" + regionGroup.getNum() + ":" + userId);
            if (ObjectUtil.isNull(object)) {
                throw new GlobalException("您当前未加入当前地区群聊或在线时长已到期");
            }
        } else {
            RegionGroupMember regionGroupMember = regionGroupMemberService
                    .findByRegionGroupIdAndUserId(regionGroup.getId(), userId);
            if (ObjectUtil.isNull(regionGroupMember) || regionGroupMember.getQuit()) {
                throw new GlobalException("您已不在当前地区群聊里面，无法发送消息");
            }
        }
    }

    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        if(!imClient.isOnline(session.getUserId())){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "网络连接失败，无法拉取离线消息");
        }
        // 查询用户常驻的地区群聊
        List<RegionGroupMember> members = regionGroupMemberService.findNoQuitByUserId(session.getUserId());
        Map<Long, RegionGroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, RegionGroupMember::getRegionGroupId);
        Set<Long> groupIds = groupMemberMap.keySet();
        Set<Long> allGroupIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(groupIds)) {
            allGroupIds.addAll(groupIds);
        }
        // 查询用户临时加入的敌群群聊
        Collection<String> keys = redisCache.keys(IMRedisKey.IM_USER_TEMP_REGION_GROUP + session.getUserId() + ":*");
        for (String key : keys) {
            Object object = redisCache.getCacheObject(key);
            if (ObjectUtil.isNull(object)) {
                continue;
            }
            RegionGroup regionGroup = Convert.convert(RegionGroup.class, object);
            allGroupIds.add(regionGroup.getId());
        }

        if(CollectionUtil.isEmpty(allGroupIds)){
            // 关闭加载中标志
            this.sendLoadingMessage(false);
            return;
        }
        // 开启加载中标志
        this.sendLoadingMessage(true);
        // 只能拉取最近1个月的,最多拉取1000条
        Date minDate = DateUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<RegionGroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(RegionGroupMessage::getId, minId)
                .gt(RegionGroupMessage::getSendTime, minDate)
                .in(RegionGroupMessage::getRegionGroupId, allGroupIds)
                .ne(RegionGroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByAsc(RegionGroupMessage::getId).last("limit 1000");
        List<RegionGroupMessage> messages = this.list(wrapper);
        // 通过群聊对消息进行分组
        Map<Long, List<RegionGroupMessage>> messageGroupMap = messages.stream().collect(Collectors.groupingBy(RegionGroupMessage::getRegionGroupId));

        // 推送消息
        AtomicInteger sendCount = new AtomicInteger();
        messageGroupMap.forEach((groupId, groupMessages) -> {
            // 填充消息状态
            String key = StrUtil.join(":", RedisKey.IM_REGION_GROUP_READED_POSITION, groupId);
            Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
            long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());
            Map<Object, Object> maxIdMap = null;
            for(RegionGroupMessage m : groupMessages){
                // 排除加群之前的消息
                RegionGroupMember member = groupMemberMap.get(m.getRegionGroupId());
                if(ObjectUtil.isNotNull(member) && DateUtil.compare(member.getCreateTime(), m.getSendTime()) > 0){
                    continue;
                }
                // 排除不需要接收的消息
                List<String> recvIds = CommaTextUtils.asList(m.getRecvIds());
                if(!recvIds.isEmpty() && !recvIds.contains(session.getUserId().toString())){
                    continue;
                }
                // 组装vo
                RegionGroupMessageVO vo = BeanUtils.copyProperties(m, RegionGroupMessageVO.class);
                // 被@用户列表
                List<String> atIds = CommaTextUtils.asList(m.getAtUserIds());
                vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                // 填充状态
                vo.setStatus(readedMaxId >= m.getId() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code());
                // 针对回执消息填充已读人数
                if(m.getReceipt()){
                    if(Objects.isNull(maxIdMap)) {
                        maxIdMap = redisTemplate.opsForHash().entries(key);
                    }
                    int count = getReadedUserIds(maxIdMap, m.getId(),m.getSendId()).size();
                    vo.setReadedCount(count);
                }
                // 推送
                IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
                sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                sendMessage.setSendResult(false);
                sendMessage.setSendToSelf(false);
                sendMessage.setData(vo);
                imClient.sendRegionGroupMessage(sendMessage);
                sendCount.getAndIncrement();
            }
        });
        // 关闭加载中标志
        this.sendLoadingMessage(false);
        log.info("拉取离线地区群聊消息,用户id:{},数量:{}",session.getUserId(),sendCount.get());
    }

    @Override
    public void readedMessage(Long regionGroupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        LambdaQueryWrapper<RegionGroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RegionGroupMessage::getRegionGroupId, regionGroupId)
                .orderByDesc(RegionGroupMessage::getId)
                .last("limit 1")
                .select(RegionGroupMessage::getId);
        RegionGroupMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端,同步清空会话列表中的未读
        RegionGroupMessageVO msgInfo = new RegionGroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setRegionGroupId(regionGroupId);
        IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendRegionGroupMessage(sendMessage);

        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_REGION_GROUP_READED_POSITION, regionGroupId);
        // 原来的已读消息位置
        Object maxReadedId = redisTemplate.opsForHash().get(key, session.getUserId().toString());
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), message.getId());
        // 推送消息回执，刷新已读人数显示
        wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RegionGroupMessage::getRegionGroupId, regionGroupId);
        wrapper.gt(!Objects.isNull(maxReadedId), RegionGroupMessage::getId, maxReadedId);
        wrapper.le(!Objects.isNull(maxReadedId), RegionGroupMessage::getId, message.getId());
        wrapper.ne(RegionGroupMessage::getStatus, MessageStatus.RECALL.code());
        wrapper.eq(RegionGroupMessage::getReceipt, true);
        List<RegionGroupMessage> receiptMessages = this.list(wrapper);
        if (CollectionUtil.isNotEmpty(receiptMessages)) {
            List<Long> userIds = regionGroupMemberService.findUserIdsByRegionGroupId(regionGroupId);
            Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
            for (RegionGroupMessage receiptMessage : receiptMessages) {
                Integer readedCount = getReadedUserIds(maxIdMap, receiptMessage.getId(),receiptMessage.getSendId()).size();
                // 如果所有人都已读，记录回执消息完成标记
                if(readedCount >= userIds.size() - 1){
                    receiptMessage.setReceiptOk(true);
                    this.updateById(receiptMessage);
                }
                msgInfo = new RegionGroupMessageVO();
                msgInfo.setId(receiptMessage.getId());
                msgInfo.setRegionGroupId(regionGroupId);
                msgInfo.setReadedCount(readedCount);
                msgInfo.setReceiptOk(receiptMessage.getReceiptOk());
                msgInfo.setType(MessageType.RECEIPT.code());;
                sendMessage = new IMRegionGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
                sendMessage.setRecvIds(userIds);
                sendMessage.setData(msgInfo);
                sendMessage.setSendToSelf(false);
                sendMessage.setSendResult(false);
                imClient.sendRegionGroupMessage(sendMessage);
            }
        }
    }

    @Override
    public List<Long> findReadedUsers(Long regionGroupId, Long messageId) {
        UserSession session = SessionContext.getSession();
        RegionGroupMessage message = this.getById(messageId);
        if (Objects.isNull(message)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        // 是否在群聊里面
        RegionGroupMember member = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroupId, session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面");
        }

        // 已读位置key
        String key = StrUtil.join(":", RedisKey.IM_REGION_GROUP_READED_POSITION, regionGroupId);
        // 一次获取所有用户的已读位置
        Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
        // 返回已读用户的id集合
        return getReadedUserIds(maxIdMap, message.getId(),message.getSendId());
    }

    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        RegionGroupMessage msg = this.getById(id);
        if (Objects.isNull(msg)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        if (!msg.getSendId().equals(session.getUserId())) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "这条消息不是由您发送,无法撤回");
        }
        if (System.currentTimeMillis() - msg.getSendTime().getTime() > IMConstant.ALLOW_RECALL_SECOND * 1000) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息已发送超过5分钟，无法撤回");
        }
        // 判断是否在群里
        RegionGroupMember member = regionGroupMemberService.findByRegionGroupIdAndUserId(msg.getRegionGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 群发
        List<Long> userIds = regionGroupMemberService.findUserIdsByRegionGroupId(msg.getRegionGroupId());
        // 不用发给自己
        userIds = userIds.stream().filter(uid -> !session.getUserId().equals(uid)).collect(Collectors.toList());
        RegionGroupMessageVO msgInfo = BeanUtils.copyProperties(msg, RegionGroupMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        String content = String.format("'%s'撤回了一条消息", member.getAliasName());
        msgInfo.setContent(content);
        msgInfo.setSendTime(new Date());

        IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendRegionGroupMessage(sendMessage);

        // 推给自己其他终端
//        msgInfo.setContent("你撤回了一条消息");
//        sendMessage.setSendToSelf(true);
//        sendMessage.setRecvIds(Collections.emptyList());
//        sendMessage.setRecvTerminals(Collections.emptyList());
//        imClient.sendRegionGroupMessage(sendMessage);
        log.info("撤回地区群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getRegionGroupId(), msg.getContent());
    }

    @Override
    public List<RegionGroupMessageVO> findHistoryMessage(Long regionGroupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        RegionGroupMember member = regionGroupMemberService.findByRegionGroupIdAndUserId(regionGroupId, userId);
        if (member == null || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<RegionGroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RegionGroupMessage::getRegionGroupId, regionGroupId)
                .ge(RegionGroupMessage::getSendTime, member.getCreateTime())
                .ne(RegionGroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(RegionGroupMessage::getId)
                .last("limit " + stIdx + "," + size);

        List<RegionGroupMessage> messages = this.list(wrapper);
        List<RegionGroupMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, RegionGroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取地区群聊记录，用户id:{},群聊id:{}，数量:{}", userId, regionGroupId, messageInfos.size());
        return messageInfos;
    }

    private List<Long> getReadedUserIds(Map<Object, Object> maxIdMap, Long messageId, Long sendId) {
        List<Long> userIds = new LinkedList<>();
        maxIdMap.forEach((k, v) -> {
            Long userId = Long.valueOf(k.toString());
            long maxId = Long.valueOf(v.toString());
            // 发送者不计入已读人数
            if (!sendId.equals(userId) && maxId >= messageId) {
                userIds.add(userId);
            }
        });
        return userIds;
    }

    private void sendLoadingMessage(Boolean isLoading){
        UserSession session = SessionContext.getSession();
        RegionGroupMessageVO msgInfo = new RegionGroupMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendRegionGroupMessage(sendMessage);
    }
}
