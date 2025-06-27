package xyz.qy.implatform.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.GroupMsgReadPosition;
import xyz.qy.implatform.mapper.GroupMsgReadPositionMapper;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @description: 公共定时任务
 * @author: Polaris
 * @create: 2023-04-29 10:19
 **/
@Slf4j
@Component
public class CommonScheduledTask {
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMediaMaterialService mediaMaterialService;

    @Autowired
    private IMusicService musicService;

    @Resource
    private GroupMsgReadPositionMapper groupMsgReadPositionMapper;

    @Scheduled(cron = "0 1 0 * * ?", zone = "Asia/Shanghai")
    public void clear() {
        // 清空redis访客记录
        redisCache.deleteObject(RedisKey.UNIQUE_VISITOR);
    }

    @Scheduled(cron = "0 0 6 1/2 * ?", zone = "Asia/Shanghai")
    public void generateRandomSort() {
        mediaMaterialService.generateRandomSort();
    }

    /**
     * 记录用户群聊消息已读位置
     */
    @Scheduled(cron = "0 50 23 * * ?", zone = "Asia/Shanghai")
    public void recordGroupMsgReadPosition() {
        Collection<String> keys = redisCache.keys(RedisKey.IM_GROUP_READED_POSITION + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        List<GroupMsgReadPosition> list = new LinkedList<>();
        GroupMsgReadPosition position = null;
        for (String key : keys) {
            Object value = redisCache.getCacheObject(key);
            String[] arr = key.split(":");
            if (arr.length < 6) {
                continue;
            }
            position = new GroupMsgReadPosition();
            Long groupId = Long.parseLong(arr[4]);
            Long userId = Long.parseLong(arr[5]);
            Long groupMsgId = Long.parseLong(value.toString());
            position.setGroupId(groupId);
            position.setUserId(userId);
            position.setGroupMsgId(groupMsgId);
            list.add(position);
        }
        groupMsgReadPositionMapper.batchSaveOrUpdate(list);
    }
}
