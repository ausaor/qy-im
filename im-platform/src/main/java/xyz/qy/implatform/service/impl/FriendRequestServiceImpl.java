package xyz.qy.implatform.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.imclient.annotation.Lock;
import xyz.qy.implatform.entity.FriendRequest;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.FriendRequestStatusEnum;
import xyz.qy.implatform.enums.MessageType;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.FriendRequestMapper;
import xyz.qy.implatform.service.IFriendRequestService;
import xyz.qy.implatform.service.IFriendService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.FriendRequestVO;

import java.util.Collections;
import java.util.List;

/**
 * 请求添加好友表 服务实现类
 *
 * @author Polaris
 * @since 2025-08-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements IFriendRequestService {

    private final IFriendService friendService;

    @Override
    public List<FriendRequestVO> friendRequestList() {
        Long userId = SessionContext.getSession().getUserId();
        // 查询待处理的申请，发送人或接收人是自己
        LambdaQueryWrapper<FriendRequest> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper
                .eq(FriendRequest::getStatus, FriendRequestStatusEnum.APPLYING.getCode())
                .and(qw -> qw.like(FriendRequest::getRecvId, userId)
                                .or()
                                .like(FriendRequest::getSendId, userId));
        List<FriendRequest> friendRequests = this.list(queryWrapper);
        if (CollectionUtil.isEmpty(friendRequests)) {
            return Collections.emptyList();
        }
        return BeanUtils.copyProperties(friendRequests, FriendRequestVO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Lock(prefix = "im:friend:request", key = "#id")
    @Override
    public void approve(Long id) {
        Long userId = SessionContext.getSession().getUserId();
        FriendRequest friendRequest = this.getById(id);
        if (ObjectUtil.isNull(friendRequest)) {
            throw new GlobalException("数据不存在");
        }
        if (!userId.equals(friendRequest.getRecvId())) {
            throw new GlobalException("不是您的验证数据");
        }
        if (!FriendRequestStatusEnum.APPLYING.getCode().equals(friendRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }

        friendRequest.setStatus(FriendRequestStatusEnum.AGREED.getCode());
        this.updateById(friendRequest);


        friendService.bindFriend(userId, friendRequest.getSendId());
        friendService.bindFriend(friendRequest.getSendId(), userId);

        friendService.sendAddTipMessage(friendRequest.getSendId());
        friendService.sendFriendRequestMessage(friendRequest, MessageType.FRIEND_REQUEST_MODIFY.code());
    }

    @Lock(prefix = "im:friend:request", key = "#id")
    @Override
    public void reject(Long id) {
        Long userId = SessionContext.getSession().getUserId();
        FriendRequest friendRequest = this.getById(id);
        if (ObjectUtil.isNull(friendRequest)) {
            throw new GlobalException("数据不存在");
        }
        if (!userId.equals(friendRequest.getRecvId())) {
            throw new GlobalException("不是您的验证数据");
        }
        if (!FriendRequestStatusEnum.APPLYING.getCode().equals(friendRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }
        friendRequest.setStatus(FriendRequestStatusEnum.REFUSED.getCode());
        this.updateById(friendRequest);
        friendService.sendFriendRequestMessage(friendRequest, MessageType.FRIEND_REQUEST_MODIFY.code());
    }

    @Lock(prefix = "im:friend:request", key = "#id")
    @Override
    public void recall(Long id) {
        Long userId = SessionContext.getSession().getUserId();
        FriendRequest friendRequest = this.getById(id);
        if (ObjectUtil.isNull(friendRequest)) {
            throw new GlobalException("数据不存在");
        }
        if (!userId.equals(friendRequest.getSendId())) {
            throw new GlobalException("不是您的数据,无权限操作");
        }
        if (!FriendRequestStatusEnum.APPLYING.getCode().equals(friendRequest.getStatus())) {
            throw new GlobalException("当前数据状态不能操作");
        }
        friendRequest.setStatus(FriendRequestStatusEnum.RECALL.getCode());
        this.updateById(friendRequest);
        friendService.sendFriendRequestMessage(friendRequest, MessageType.FRIEND_REQUEST_MODIFY.code());
    }
}
