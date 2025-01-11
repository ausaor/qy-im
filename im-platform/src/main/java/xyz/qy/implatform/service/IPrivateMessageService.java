package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.entity.PrivateMessage;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.List;


public interface IPrivateMessageService extends IService<PrivateMessage> {

    PrivateMessageVO sendMessage(PrivateMessageDTO dto);

    void sendPrivateMessage(PrivateMessageDTO dto, Long sendUserId);

    void recallMessage(Long id);

    List<PrivateMessageVO> findHistoryMessage(Long friendId, Long page,Long size);

    /**
     * 拉取离线消息，只能拉取最近1个月的消息，最多拉取1000条
     *
     * @param minId 消息起始id
     */
    void pullOfflineMessage(Long minId);

    void readedMessage(Long friendId);

    /**
     *  获取某个会话中已读消息的最大id
     *
     * @param friendId 好友id
     */
    Long getMaxReadedId(Long friendId);
}
