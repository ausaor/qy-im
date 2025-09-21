package xyz.qy.implatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xyz.qy.imclient.IMClient;
import xyz.qy.imcommon.model.IMSystemMessage;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.SysMsgQueryDTO;
import xyz.qy.implatform.dto.SystemMessageDTO;
import xyz.qy.implatform.entity.Pusher;
import xyz.qy.implatform.entity.SystemMessage;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.MessageStatus;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.SystemMessageMapper;
import xyz.qy.implatform.service.IPushTaskService;
import xyz.qy.implatform.service.IPusherService;
import xyz.qy.implatform.service.ISystemMessageService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.SystemMessageVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统消息
 *
 * @author Polaris
 * @since 2024-12-28
 */
@Slf4j
@Service
public class SystemMessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage> implements ISystemMessageService {
    @Resource
    private IMClient imClient;

    @Resource
    private SystemMessageMapper systemMessageMapper;

    @Resource
    private IPusherService pusherService;

    @Resource
    private IPushTaskService pushTaskService;

    @Resource
    private IUserService userService;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(SystemMessageDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (!userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("只有系统管理员有权限新增系统消息");
        }

        SystemMessage systemMessage = BeanUtils.copyProperties(dto, SystemMessage.class);
        systemMessage.setCreateBy(userId);

        this.save(systemMessage);
    }

    @Override
    public void pullOfflineMessage(Long minSeqNo) {
        UserSession session = SessionContext.getSession();
        if(!imClient.isOnline(session.getUserId())){
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "网络连接失败，无法拉取离线消息");
        }

        Long userId = session.getUserId();
        //this.sendLoadingMessage(true);
        Date startTime = DateUtils.addMonths(new Date(), -1);

        List<Pusher> pushers = pusherService.list();
        Map<Long, Pusher> pusherMap = pushers.stream().collect(Collectors.toMap(Pusher::getId, Function.identity(), (key1, key2) -> key2));
        List<SystemMessageVO> systemMessageVOS = systemMessageMapper.pullOfflineSystemMessage(startTime, userId, minSeqNo);

        // 通过推送主体对消息进行分组
        Map<Long, List<SystemMessageVO>> messageSystemMap = systemMessageVOS.stream().collect(Collectors.groupingBy(SystemMessageVO::getPusherId));
        AtomicInteger sendCount = new AtomicInteger();
        messageSystemMap.forEach((pusherId, systemMessageVOList) ->{
            // 填充消息状态
            String key = StrUtil.join(":", RedisKey.IM_SYSTEM_READED_POSITION, pusherId);
            Object o = redisTemplate.opsForHash().get(key, session.getUserId().toString());
            long readedMaxId = Objects.isNull(o) ? -1 : Long.parseLong(o.toString());

            // 推送消息

            systemMessageVOList.forEach(item -> {
                IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
                Pusher pusher = pusherMap.get(item.getPusherId());
                if (ObjectUtil.isNotNull(pusher)) {
                    item.setPusherName(pusher.getName());
                    item.setPusherHeadImage(pusher.getHeadImage());
                }
                // 填充状态
                item.setStatus(readedMaxId >= item.getSeqNo() ? MessageStatus.READED.code() : MessageStatus.UNSEND.code());
                sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
                sendMessage.setSendResult(false);
                sendMessage.setData(item);
                imClient.sendSystemMessage(sendMessage);
                sendCount.getAndIncrement();
            });
        });
        
        // 关闭加载中标志
        this.sendLoadingMessage(false);
        log.info("拉取离线系统消息,用户id:{},数量:{}", session.getUserId(), sendCount.get());
    }

    @Override
    public PageResultVO pageSysMsg(SysMsgQueryDTO queryDTO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        if (!userId.equals(Constant.ADMIN_USER_ID)) {
            throw new GlobalException("不是系统管理员，无法操作");
        }

        LambdaQueryWrapper<SystemMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SystemMessage::getDeleted, false);
        queryWrapper.eq(queryDTO.getType() != null, SystemMessage::getType, queryDTO.getType());
        queryWrapper.like(StringUtils.isNotBlank(queryDTO.getTitle()), SystemMessage::getTitle, queryDTO.getTitle());
        queryWrapper.orderByDesc(SystemMessage::getId);

        Page<SystemMessage> page = new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize());
        Page<SystemMessage> pageResult = this.page(page, queryWrapper);

        List<SystemMessage> systemMessageList = pageResult.getRecords();
        if (CollUtil.isEmpty(systemMessageList)) {
            return PageResultVO.builder().data(Collections.emptyList()).build();
        }
        List<Long> userIds = systemMessageList.stream().map(SystemMessage::getCreateBy).collect(Collectors.toList());
        List<User> userList = userService.findUserByIds(userIds);
        // userList根据id为key转换成map
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        List<SystemMessageVO> systemMessageVOS = BeanUtils.copyPropertiesList(systemMessageList, SystemMessageVO.class);
        systemMessageVOS.forEach(item -> {
            item.setCreateByName(userMap.get(item.getCreateBy()).getNickName());
        });

        return PageResultVO.builder().data(systemMessageVOS).total(page.getTotal()).build();
    }

    @Override
    public void readedMessage(Long pusherId) {
        UserSession session = SessionContext.getSession();
        // 取出最后的消息id
        Integer seqNo = systemMessageMapper.selectLastReadedSeqNo(pusherId, session.getUserId());
        if (Objects.isNull(seqNo)) {
            return;
        }

        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.READED.code());
        msgInfo.setPusherId(pusherId);
        msgInfo.setSendTime(new Date());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);

        // 已读消息key
        String key = StrUtil.join(":", RedisKey.IM_SYSTEM_READED_POSITION, pusherId);
        // 记录已读消息位置
        redisTemplate.opsForHash().put(key, session.getUserId().toString(), seqNo);
    }

    private void sendLoadingMessage(Boolean isLoading){
        UserSession session = SessionContext.getSession();
        SystemMessageVO msgInfo = new SystemMessageVO();
        msgInfo.setType(MessageType.LOADING.code());
        msgInfo.setContent(isLoading.toString());
        IMSystemMessage<SystemMessageVO> sendMessage = new IMSystemMessage<>();
        sendMessage.setRecvIds(Collections.singletonList(session.getUserId()));
        sendMessage.setRecvTerminals(Collections.singletonList(session.getTerminal()));
        sendMessage.setData(msgInfo);
        sendMessage.setSendResult(false);
        imClient.sendSystemMessage(sendMessage);
    }
}
