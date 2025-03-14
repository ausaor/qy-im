package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xyz.qy.implatform.entity.GroupMsgReadPosition;

import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-09-09 20:14
 **/
public interface GroupMsgReadPositionMapper extends BaseMapper<GroupMsgReadPosition> {
    int batchSaveOrUpdate(List<GroupMsgReadPosition> list);
}
