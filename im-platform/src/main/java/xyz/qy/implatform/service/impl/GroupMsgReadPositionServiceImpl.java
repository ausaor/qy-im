package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.GroupMsgReadPosition;
import xyz.qy.implatform.mapper.GroupMsgReadPositionMapper;
import xyz.qy.implatform.service.IGroupMsgReadPositionService;

/**
 * @description:
 * @author: Polaris
 * @create: 2024-01-28 17:13
 **/
@Slf4j
@Service
public class GroupMsgReadPositionServiceImpl extends ServiceImpl<GroupMsgReadPositionMapper, GroupMsgReadPosition> implements IGroupMsgReadPositionService {
    @Override
    public Integer getGroupMsgReadId(Long groupId, Long userId) {
        Integer maxReadedId = null;
        GroupMsgReadPosition groupMsgReadPosition = baseMapper.selectOne(new LambdaQueryWrapper<GroupMsgReadPosition>()
                .eq(GroupMsgReadPosition::getGroupId, groupId)
                .eq(GroupMsgReadPosition::getUserId, userId));
        if (ObjectUtil.isNotNull(groupMsgReadPosition)) {
            maxReadedId = groupMsgReadPosition.getGroupMsgId().intValue();
        }
        return maxReadedId;
    }
}
