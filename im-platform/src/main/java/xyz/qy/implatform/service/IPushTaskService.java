package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.PushTaskDTO;
import xyz.qy.implatform.entity.PushTask;

public interface IPushTaskService extends IService<PushTask> {
    /**
     * 推送系统消息
     *
     * @param dto 入参
     */
    void pushSystemMessage(PushTaskDTO dto);
}
