package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
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
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imcommon.util.CommaTextUtils;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.entity.GroupMsgReadPosition;
import xyz.qy.implatform.enums.GroupTypeEnum;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.GroupMessageMapper;
import xyz.qy.implatform.mapper.GroupMsgReadPositionMapper;
import xyz.qy.implatform.service.ICharacterWordService;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IGroupMessageService;
import xyz.qy.implatform.service.IGroupMsgReadPositionService;
import xyz.qy.implatform.service.IGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.MsgTypeUtil;
import xyz.qy.implatform.util.SensitiveUtil;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.QuoteMsg;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupMessageServiceImpl extends ServiceImpl<GroupMessageMapper, GroupMessage> implements IGroupMessageService {
    @Resource
    private IGroupService groupService;

    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IMClient imClient;

    @Resource
    private GroupMsgReadPositionMapper groupMsgReadPositionMapper;

    @Resource
    private IGroupMsgReadPositionService groupMsgReadPositionService;

    @Resource
    private ICharacterWordService characterWordService;

    /**
     * 发送群聊消息(高并发接口，查询mysql接口都要进行缓存)
     *
     * @param dto 群聊消息
     * @return 群聊id
     */
    @Override
    public GroupMessageVO sendMessage(GroupMessageDTO dto) {
        if (!MessageType.checkGroupMsgType(dto.getType())) {
            throw new GlobalException("消息类型错误");
        }
        if (MessageType.checkMediaMsgType(dto.getType())) {
            if (!MsgTypeUtil.checkMediaMsgContent(dto.getType(), dto.getContent())) {
                throw new GlobalException("消息内容格式不正确");
            }
        }
        UserSession session = SessionContext.getSession();
        Group group = groupService.getById(dto.getGroupId());
        if (Objects.isNull(group)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊不存在");
        }
        if (group.getDeleted()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "群聊已解散");
        }
        if (!group.getVersion().equals(dto.getVersion())) {
            throw new GlobalException("群聊信息有更新，请先刷新");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(dto.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您不在群聊中，无法发送消息");
        }
        if (!group.getGroupType().equals(GroupTypeEnum.COMMON.getCode()) && ObjectUtil.isNotNull(member.getTemplateCharacterId())
                && !member.getTemplateCharacterId().equals(dto.getCharacterId())) {
            throw new GlobalException("角色参数异常");
        }
        if (MessageType.WORD_VOICE.code().equals(dto.getType())) {
            if (group.getGroupType().equals(GroupTypeEnum.COMMON.getCode())) {
                throw new GlobalException("当前群聊类型不支持语音台词类型消息");
            }

            Long wordId = MsgTypeUtil.getWordIdFromContent(dto.getContent());
            CharacterWord characterWord = characterWordService.getById(wordId);
            if (Objects.isNull(characterWord) || characterWord.getDeleted() || !characterWord.getStatus().equals(ReviewEnum.REVIEWED.getCode())) {
                throw new GlobalException("角色台词不存在");
            }
            if (characterWord.getCharacterId() != -1L && !member.getTemplateCharacterId().equals(characterWord.getCharacterId())) {
                throw new GlobalException("角色台词不匹配");
            }
            if (!characterWord.getTemplateGroupId().equals(member.getTemplateGroupId())) {
                throw new GlobalException("角色台词不匹配");
            }
            dto.setContent(MsgTypeUtil.formatContent(characterWord));
        }

        // 判断是否在群里
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(group.getId());
//        if (dto.getReceipt() && userIds.size() > Constant.RECEIPT_LIMIT_MEMBERS) {
//            throw new GlobalException(
//                    String.format("当前群聊大于%s人,不支持发送回执消息", Constant.RECEIPT_LIMIT_MEMBERS));
//        }
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 保存消息
        GroupMessage msg = BeanUtils.copyProperties(dto, GroupMessage.class);
        msg.setSendId(session.getUserId());
        msg.setSendTime(new Date());
        msg.setSendNickName(member.getAliasName());
        msg.setSendUserAvatar(member.getHeadImage());
        msg.setIsTemplate(member.getIsTemplate());
        if (MessageType.TEXT.code().equals(msg.getType())) {
            msg.setContent(SensitiveUtil.filter(msg.getContent()));
        }
        QuoteMsg quoteMsg1 = null;
        if (ObjectUtil.isNotNull(dto.getQuoteId())) {
            GroupMessage quoteMsg = this.getById(dto.getQuoteId());
            if (ObjectUtil.isNotNull(quoteMsg)) {
                quoteMsg1 = covertQuoteMsg(quoteMsg);
                msg.setQuoteMsg(JSON.toJSONString(quoteMsg1));
            }
        }
        msg.setAtUserIds(CommaTextUtils.asText(dto.getAtUserIds()));
        this.save(msg);
        // 不用发给自己
        userIds = userIds.stream().filter(id -> !session.getUserId().equals(id)).collect(Collectors.toList());
        // 群发
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        if (quoteMsg1 != null) {
            msgInfo.setQuoteMsg(quoteMsg1);
        }
        msgInfo.setAtUserIds(dto.getAtUserIds());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendGroupMessage(sendMessage);
        log.info("发送群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), dto.getGroupId(), dto.getContent());
        return msgInfo;
    }

    private QuoteMsg covertQuoteMsg(GroupMessage message) {
        QuoteMsg quoteMsg = new QuoteMsg();
        quoteMsg.setId(message.getId());
        quoteMsg.setContent(message.getContent());
        quoteMsg.setStatus(message.getStatus());
        quoteMsg.setType(message.getType());
        quoteMsg.setSendId(message.getSendId());
        quoteMsg.setAtUserIds(CommaTextUtils.asLongList(message.getAtUserIds()));
        quoteMsg.setSendNickName(message.getSendNickName());
        quoteMsg.setSendAvatar(message.getSendUserAvatar());
        quoteMsg.setIsTemplate(message.getIsTemplate());
        return quoteMsg;
    }

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    @Override
    public void recallMessage(Long id) {
        UserSession session = SessionContext.getSession();
        GroupMessage msg = this.getById(id);
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
        GroupMember member = groupMemberService.findByGroupAndUserId(msg.getGroupId(), session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面，无法撤回消息");
        }
        // 修改数据库
        msg.setStatus(MessageStatus.RECALL.code());
        this.updateById(msg);
        // 群发
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(msg.getGroupId());
        // 不用发给自己
        userIds = userIds.stream().filter(uid -> !session.getUserId().equals(uid)).collect(Collectors.toList());
        GroupMessageVO msgInfo = BeanUtils.copyProperties(msg, GroupMessageVO.class);
        msgInfo.setType(MessageType.RECALL.code());
        String aliasName = member.getAliasName();
        if (member.getIsTemplate()) {
            if (StrUtil.isNotBlank(member.getAliasNamePrefix())) {
                aliasName = member.getAliasNamePrefix() + aliasName;
            }
            if (StrUtil.isNotBlank(member.getAliasNameSuffix())) {
                aliasName = aliasName + member.getAliasNameSuffix();
            }
        }
        String content = String.format("'%s'撤回了一条消息", aliasName);
        msgInfo.setContent(content);
        msgInfo.setSendTime(new Date());

        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(userIds);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        sendMessage.setSendToSelf(false);
        imClient.sendGroupMessage(sendMessage);

        // 推给自己其他终端
        msgInfo.setContent("你撤回了一条消息");
        sendMessage.setSendToSelf(true);
        sendMessage.setRecvIds(Collections.emptyList());
        sendMessage.setRecvTerminals(Collections.emptyList());
        imClient.sendGroupMessage(sendMessage);
        log.info("撤回群聊消息，发送id:{},群聊id:{},内容:{}", session.getUserId(), msg.getGroupId(), msg.getContent());
    }

    private Integer getGroupMsgReadId(Long groupId, Long userId) {
        String key = String.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId.toString(), userId.toString());
        Integer maxReadedId = (Integer) redisTemplate.opsForValue().get(key);
        if (ObjectUtil.isNull(maxReadedId)) {
            GroupMsgReadPosition groupMsgReadPosition = groupMsgReadPositionMapper.selectOne(new LambdaQueryWrapper<GroupMsgReadPosition>()
                    .eq(GroupMsgReadPosition::getGroupId, groupId)
                    .eq(GroupMsgReadPosition::getUserId, userId));
            if (ObjectUtil.isNotNull(groupMsgReadPosition)) {
                maxReadedId = groupMsgReadPosition.getGroupMsgId().intValue();
            }
        }
        return maxReadedId;
    }

    @Override
    public void pullOfflineMessage(Long minId) {
        UserSession session = SessionContext.getSession();
        if(!imClient.isOnline(session.getUserId())){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "网络连接失败，无法拉取离线消息");
        }
        // 查询用户加入的群组
        List<GroupMember> members = groupMemberService.findByUserId(session.getUserId());
        Map<Long, GroupMember> groupMemberMap = CollStreamUtil.toIdentityMap(members, GroupMember::getGroupId);
        Set<Long> groupIds = groupMemberMap.keySet();
        if(CollectionUtil.isEmpty(groupIds)){
            // 关闭加载中标志
            this.sendLoadingMessage(false);
            return;
        }
        // 开启加载中标志
        //this.sendLoadingMessage(true);
        // 只能拉取最近1个月的,最多拉取1000条
        Date minDate = DateUtils.addMonths(new Date(), -1);
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(GroupMessage::getId, minId)
                .gt(GroupMessage::getSendTime, minDate)
                .in(GroupMessage::getGroupId, groupIds)
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByAsc(GroupMessage::getId).last("limit 1000");
        List<GroupMessage> messages = this.list(wrapper);
        // 通过群聊对消息进行分组
        Map<Long, List<GroupMessage>> messageGroupMap = messages.stream().collect(Collectors.groupingBy(GroupMessage::getGroupId));
        // 退群前的消息
        List<GroupMember> quitMembers = groupMemberService.findQuitInMonth(session.getUserId());
        for(GroupMember quitMember: quitMembers){
            wrapper = Wrappers.lambdaQuery();
            wrapper.gt(GroupMessage::getId, minId)
                    .between(GroupMessage::getSendTime, minDate,quitMember.getQuitTime())
                    .eq(GroupMessage::getGroupId, quitMember.getGroupId())
                    .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                    .orderByAsc(GroupMessage::getId);
            List<GroupMessage> groupMessages = this.list(wrapper);
            messageGroupMap.put(quitMember.getGroupId(),groupMessages);
            groupMemberMap.put(quitMember.getGroupId(),quitMember);
        }
        // 推送消息
        AtomicInteger sendCount = new AtomicInteger();
        messageGroupMap.forEach((groupId, groupMessages) -> {
            // 填充消息状态
            String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
            Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
            long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());
            Map<Object, Object> maxIdMap = null;
            for(GroupMessage m:groupMessages){
                // 排除加群之前的消息
                GroupMember member = groupMemberMap.get(m.getGroupId());
                if(DateUtil.compare(member.getCreatedTime(), m.getSendTime()) > 0){
                    continue;
                }
                // 排除不需要接收的消息
                List<String> recvIds = CommaTextUtils.asList(m.getRecvIds());
                if(!recvIds.isEmpty() && !recvIds.contains(session.getUserId().toString())){
                    continue;
                }
                // 组装vo
                GroupMessageVO vo = BeanUtils.copyProperties(m, GroupMessageVO.class);
                if (StrUtil.isNotBlank(m.getQuoteMsg())) {
                    vo.setQuoteMsg(JSON.parseObject(m.getQuoteMsg(), QuoteMsg.class));
                }

                // 被@用户列表
                List<String> atIds = CommaTextUtils.asList(m.getAtUserIds());
                vo.setAtUserIds(atIds.stream().map(Long::parseLong).collect(Collectors.toList()));
                // 填充状态
                vo.setStatus(readedMaxId >= m.getId() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code());
                // 针对回执消息填充已读人数
//                if(m.getReceipt()){
//                    if(Objects.isNull(maxIdMap)) {
//                        maxIdMap = redisTemplate.opsForHash().entries(key);
//                    }
//                    int count = getReadedUserIds(maxIdMap, m.getId(),m.getSendId()).size();
//                    vo.setReadedCount(count);
//                }
                // 推送
                IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
                sendMessage.setSender(new IMUserInfo(m.getSendId(), IMTerminalType.WEB.code()));
                sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
                sendMessage.setSendResult(false);
                sendMessage.setSendToSelf(false);
                sendMessage.setData(vo);
                imClient.sendGroupMessage(sendMessage);
                sendCount.getAndIncrement();
            }
        });
        // 关闭加载中标志
        this.sendLoadingMessage(false);
        log.info("拉取离线群聊消息,用户id:{},数量:{}",session.getUserId(),sendCount.get());
    }

    /**
     * 消息已读,同步其他终端，清空未读数量
     *
     * @param groupId 群聊
     */
    @Override
    public void readedMessage(Long groupId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        LambdaQueryWrapper<GroupMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GroupMessage::getGroupId, groupId)
                .orderByDesc(GroupMessage::getId)
                .last("limit 1")
                .select(GroupMessage::getId);
        GroupMessage message = this.getOne(wrapper);
        if (Objects.isNull(message)) {
            return;
        }
        // 推送消息给自己的其他终端,同步清空会话列表中的未读
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setSendTime(new Date());
        msgInfo.setSendId(session.getUserId());
        msgInfo.setGroupId(groupId);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setSendToSelf(true);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(true);
        imClient.sendGroupMessage(sendMessage);

        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 原来的已读消息位置
        //Object maxReadedId = redisTemplate.opsForHash().get(key, session.getUserId().toString());
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), message.getId());
        // 推送消息回执，刷新已读人数显示
//        wrapper = Wrappers.lambdaQuery();
//        wrapper.eq(GroupMessage::getGroupId, groupId);
//        wrapper.gt(!Objects.isNull(maxReadedId), GroupMessage::getId, maxReadedId);
//        wrapper.le(!Objects.isNull(maxReadedId), GroupMessage::getId, message.getId());
//        wrapper.ne(GroupMessage::getStatus, MessageStatus.RECALL.code());
//        wrapper.eq(GroupMessage::getReceipt, true);
//        List<GroupMessage> receiptMessages = this.list(wrapper);
//        if (CollectionUtil.isNotEmpty(receiptMessages)) {
//            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
//            Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
//            for (GroupMessage receiptMessage : receiptMessages) {
//                Integer readedCount = getReadedUserIds(maxIdMap, receiptMessage.getId(),receiptMessage.getSendId()).size();
//                // 如果所有人都已读，记录回执消息完成标记
//                if(readedCount >= userIds.size() - 1){
//                    receiptMessage.setReceiptOk(true);
//                    this.updateById(receiptMessage);
//                }
//                msgInfo = new GroupMessageVO();
//                msgInfo.setId(receiptMessage.getId());
//                msgInfo.setGroupId(groupId);
//                msgInfo.setReadedCount(readedCount);
//                msgInfo.setReceiptOk(receiptMessage.getReceiptOk());
//                msgInfo.setType(MessageType.RECEIPT.code());;
//                sendMessage = new IMGroupMessage<>();
//                sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
//                sendMessage.setRecvIds(userIds);
//                sendMessage.setData(msgInfo);
//                sendMessage.setSendToSelf(false);
//                sendMessage.setSendResult(false);
//                imClient.sendGroupMessage(sendMessage);
//            }
//        }
    }

    @Override
    public List<Long> findReadedUsers(Long groupId, Long messageId) {
        UserSession session = SessionContext.getSession();
        GroupMessage message = this.getById(messageId);
        if (Objects.isNull(message)) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "消息不存在");
        }
        // 是否在群聊里面
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, session.getUserId());
        if (Objects.isNull(member) || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊里面");
        }

        // 已读位置key
        String key = StrUtil.join(":", RedisKey.IM_GROUP_READED_POSITION, groupId);
        // 一次获取所有用户的已读位置
        Map<Object, Object> maxIdMap = redisTemplate.opsForHash().entries(key);
        // 返回已读用户的id集合
        return getReadedUserIds(maxIdMap, message.getId(),message.getSendId());
    }

    /**
     * 拉取历史聊天记录
     *
     * @param groupId 群聊id
     * @param page    页码
     * @param size    页码大小
     * @return 聊天记录列表
     */
    @Override
    public List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? size : 10;
        Long userId = SessionContext.getSession().getUserId();
        long stIdx = (page - 1) * size;
        // 群聊成员信息
        GroupMember member = groupMemberService.findByGroupAndUserId(groupId, userId);
        if (member == null || member.getQuit()) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "您已不在群聊中");
        }
        // 查询聊天记录，只查询加入群聊时间之后的消息
        QueryWrapper<GroupMessage> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(GroupMessage::getGroupId, groupId)
                .ge(GroupMessage::getSendTime, member.getCreatedTime())
                .ne(GroupMessage::getStatus, MessageStatus.RECALL.code())
                .orderByDesc(GroupMessage::getId)
                .last("limit " + stIdx + "," + size);

        List<GroupMessage> messages = this.list(wrapper);
        List<GroupMessageVO> messageInfos = messages.stream().map(m -> BeanUtils.copyProperties(m, GroupMessageVO.class)).collect(Collectors.toList());
        log.info("拉取群聊记录，用户id:{},群聊id:{}，数量:{}", userId, groupId, messageInfos.size());
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
        GroupMessageVO msgInfo = new GroupMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
    }
}
