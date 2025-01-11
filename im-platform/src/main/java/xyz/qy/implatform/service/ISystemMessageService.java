package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.SystemMessageDTO;
import xyz.qy.implatform.entity.SystemMessage;

public interface ISystemMessageService extends IService<SystemMessage> {
    /**
     * 保存系统消息
     *
     * @param dto 系统消息
     */
    void save(SystemMessageDTO dto);

    /**
     * 拉取离线系统消息
     *
     * @param minSeqNo 已读序号
     */
    void pullOfflineMessage(Long minSeqNo);

    /**
     * 记录已读位置
     *
     * @param pusherId 推送主体id
     */
    void readedMessage(Long pusherId);
}
