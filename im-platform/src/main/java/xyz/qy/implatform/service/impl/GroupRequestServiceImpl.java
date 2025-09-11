package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.dto.EnterGroupUsersDTO;
import xyz.qy.implatform.entity.GroupRequest;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.GroupRequestStatusEnum;
import xyz.qy.implatform.mapper.GroupRequestMapper;
import xyz.qy.implatform.service.IGroupRequestService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.vo.InviteFriendVO;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 群组请求表 服务实现类
 *
 * @author Polaris
 * @since 2025-09-11
 */
@Slf4j
@Service
public class GroupRequestServiceImpl extends ServiceImpl<GroupRequestMapper, GroupRequest> implements IGroupRequestService {
    @Resource
    private IUserService userService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveEnterGroupRequestInfo(EnterGroupUsersDTO enterGroupUsersDTO) {
        if (CollectionUtils.isEmpty(enterGroupUsersDTO.getReviewUserList())) {
            return;
        }
        List<InviteFriendVO> reviewUserList = enterGroupUsersDTO.getReviewUserList();
        List<Long> userIds = reviewUserList.stream().map(InviteFriendVO::getFriendId).collect(Collectors.toList());
        List<User> userList = userService.findUserByIds(userIds);
        // userList根据id为key转换成map
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        List<GroupRequest> groupRequestList = reviewUserList.stream().map(item -> {
            GroupRequest groupRequest = new GroupRequest();
            groupRequest.setLaunchUserId(enterGroupUsersDTO.getLaunchUserId());
            groupRequest.setUserId(item.getFriendId());
            groupRequest.setUserNickname(userMap.get(item.getFriendId()).getUserName());
            groupRequest.setUserHeadImage(userMap.get(item.getFriendId()).getHeadImage());
            groupRequest.setTemplateCharacterId(item.getTemplateCharacterId());
            groupRequest.setGroupId(enterGroupUsersDTO.getGroupId());
            groupRequest.setType(2);
            groupRequest.setStatus(GroupRequestStatusEnum.APPLYING.getCode());
            groupRequest.setCreateTime(LocalDateTime.now());
            return groupRequest;
        }).collect(Collectors.toList());

        this.saveBatch(groupRequestList);
        log.info("邀请进入群组待用户同意信息,groupId:{}, userIds:{}", enterGroupUsersDTO.getGroupId(), userIds);
    }
}
