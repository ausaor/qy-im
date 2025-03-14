package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.model.IMSystemMessage;
import xyz.qy.imcommon.model.IMUserInfo;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.PushTaskDTO;
import xyz.qy.implatform.entity.PushTask;
import xyz.qy.implatform.entity.PushTaskReceiver;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.entity.SystemMessage;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.PushTaskMapper;
import xyz.qy.implatform.service.IPushTaskReceiverService;
import xyz.qy.implatform.service.IPushTaskService;
import xyz.qy.implatform.service.IPusherService;
import xyz.qy.implatform.service.ISystemMessageService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.SystemMessageVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推送任务
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Slf4j
@Service
public class PushTaskServiceImpl extends ServiceImpl<PushTaskMapper, PushTask> implements IPushTaskService {
    @Resource
    private ISystemMessageService systemMessageService;

    @Resource
    private IPushTaskReceiverService pushTaskReceiverService;

    @Resource
    private IPusherService pusherService;

    @Resource
    private IUserService userService;

    @Resource
    private IMClient imClient;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pushSystemMessage(PushTaskDTO dto) {
        UserSession session = SessionContext.getSession();
        /*if (!session.getUserId().equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员才有权限操作");
        }*/

        // 判断系统消息是否存在
        SystemMessage systemMessage = systemMessageService.getById(dto.getSysMsgId());
        if (ObjectUtil.isNull(systemMessage) || systemMessage.getDeleted()) {
            throw new GlobalException("要推送的系统消息不存在");
        }

        // 判断推送主体
        Pusher pusher = pusherService.getById(dto.getPusherId());
        if (ObjectUtil.isNull(pusher) || pusher.getDeleted()) {
            throw new GlobalException("推送主体不存在");
        }

        PushTask pushTask = BeanUtils.copyProperties(dto, PushTask.class);
        if (ObjectUtil.isNull(dto.getSendTime())) {
            pushTask.setSendTime(new Date());
        }
        pushTask.setPushStatus(1);
        pushTask.setCreateTime(new Date());
        pushTask.setCreateBy(Constant.ADMIN_USER_ID);
        this.save(pushTask);

        SystemMessageVO msgInfo = BeanUtils.copyProperties(systemMessage, SystemMessageVO.class);
        msgInfo.setSeqNo(pushTask.getSeqNo());
        msgInfo.setStatus(pushTask.getStatus());
        msgInfo.setSendTime(pushTask.getSendTime());

        msgInfo.setPusherId(pushTask.getPusherId());
        msgInfo.setPusherName(pusher.getName());
        msgInfo.setPusherHeadImage(pusher.getHeadImage());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage();
        if ("part".equals(dto.getRecvRange()) && CollectionUtils.isNotEmpty(dto.getRecvUserIds())) {
            List<PushTaskReceiver> receiverList = dto.getRecvUserIds().stream().map(userId -> {
                PushTaskReceiver receiver = new PushTaskReceiver();
                receiver.setUserId(userId);
                receiver.setSeqNo(pushTask.getSeqNo());
                return receiver;
            }).collect(Collectors.toList());
            pushTaskReceiverService.saveBatch(receiverList);
            sendMessage.setRecvIds(dto.getRecvUserIds());
        } else {
            // 发给所有人
            List<Long> allUserIds = userService.getAllUserIds();
            sendMessage.setRecvIds(allUserIds);
        }
        sendMessage.setSendResult(false);
        sendMessage.setData(msgInfo);
        imClient.sendSystemMessage(sendMessage);
        log.info("推送系统消息，推送主体id:{},推送主体名称:{},系统消息id:{}", pusher.getId(), pusher.getName(), systemMessage.getId());
    }
}
