package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.GroupMsgReadPosition;

/**
 * @description:
 * @author: Polaris
 * @create: 2024-01-28 17:11
 **/
public interface IGroupMsgReadPositionService extends IService<GroupMsgReadPosition> {
    Integer getGroupMsgReadId(Long groupId, Long userId);
}
