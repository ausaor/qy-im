package xyz.qy.implatform.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Component;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.contant.IMRedisKey;
import xyz.qy.imcommon.enums.IMTerminalType;
import xyz.qy.imcommon.model.IMGroupMessage;
import xyz.qy.imcommon.model.IMRegionGroupMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.imcommon.util.CommaTextUtils;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.entity.RegionGroupMessage;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.mapper.GroupMessageMapper;
import xyz.qy.implatform.mapper.RegionGroupMessageMapper;
import xyz.qy.implatform.service.IGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupMemberService;
import xyz.qy.implatform.service.IRegionGroupService;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.RegionGroupMessageVO;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 消息发送工具
 *
 * @author Polaris
 * @since 2024-10-20
 */
@Component
public class MessageSendUtil {
    @Resource
    private GroupMessageMapper groupMessageMapper;

    @Resource
    private IGroupMemberService groupMemberService;

    @Resource
    private RegionGroupMessageMapper regionGroupMessageMapper;

    @Resource
    private IRegionGroupMemberService regionGroupMemberService;

    @Resource
    private IRegionGroupService regionGroupService;

    @Resource
    private IMClient imClient;

    @Resource
    private RedisCache redisCache;

    public void sendTipMessage(Long groupId, Long sendId, String sendNickName,
                               List<Long> recvIds, String content, Integer groupChangeType) {
        // 消息入库
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.UNSEND.code());
        message.setSendTime(new Date());
        message.setSendNickName(sendNickName);
        message.setGroupId(groupId);
        message.setSendId(sendId);
        if (CollUtil.isNotEmpty(recvIds)) {
            message.setRecvIds(CommaTextUtils.asText(recvIds));
        }

        groupMessageMapper.insert(message);
        // 推送
        GroupMessageVO msgInfo = BeanUtils.copyProperties(message, GroupMessageVO.class);
        IMGroupMessage<GroupMessageVO> sendMessage = new IMGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(sendId, IMTerminalType.WEB.code()));
        if (CollUtil.isEmpty(recvIds)) {
            // 为空表示向全体发送
            List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
            sendMessage.setRecvIds(userIds);
        } else {
            sendMessage.setRecvIds(recvIds);
        }
        msgInfo.setGroupChangeType(groupChangeType);
        sendMessage.setData(msgInfo);
        sendMessage.setSendToSelf(false);
        sendMessage.setSendResult(false);
        imClient.sendGroupMessage(sendMessage);
    }

    public void sendRegionGroupTipMsg(Long regionGroupId, Long sendId, String sendNickName,
                               List<Long> recvIds, String content, Integer groupChangeType) {
        // 消息入库
        RegionGroupMessage message = new RegionGroupMessage();
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setStatus(MessageStatus.UNSEND.code());
        message.setSendTime(new Date());
        message.setSendNickName(sendNickName);
        message.setRegionGroupId(regionGroupId);
        message.setSendId(sendId);

        regionGroupMessageMapper.insert(message);
        // 推送
        RegionGroupMessageVO msgInfo = BeanUtils.copyProperties(message, RegionGroupMessageVO.class);
        msgInfo.setGroupChangeType(groupChangeType);
        IMRegionGroupMessage<RegionGroupMessageVO> sendMessage = new IMRegionGroupMessage<>();
        sendMessage.setSender(new IMUserInfo(sendId, IMTerminalType.WEB.code()));
        if (CollUtil.isEmpty(recvIds)) {
            // 为空表示向全体发送
            List<Long> userIds = regionGroupMemberService.findUserIdsByRegionGroupId(regionGroupId);

            RegionGroup regionGroup = regionGroupService.getById(regionGroupId);
            Collection<String> keys = redisCache.keys(IMRedisKey.IM_REGION_GROUP_NUM_TEMP_USER + regionGroup.getCode() + ":" + regionGroup.getNum() + ":*");
            for (String key : keys) {
                Object object = redisCache.getCacheObject(key);
                if (ObjectUtil.isNull(object)) {
                    continue;
                }
                UserSession userSession = Convert.convert(UserSession.class, object);
                userIds.add(userSession.getUserId());
            }
            sendMessage.setRecvIds(userIds);
        } else {
            sendMessage.setRecvIds(recvIds);
        }
        sendMessage.setSendToSelf(false);
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendRegionGroupMessage(sendMessage);
    }
}
