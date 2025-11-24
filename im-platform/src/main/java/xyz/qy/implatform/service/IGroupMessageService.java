package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.entity.GroupMessage;
import xyz.qy.implatform.vo.GroupMessageVO;

import java.util.List;


public interface IGroupMessageService extends IService<GroupMessage> {


    GroupMessageVO sendMessage(GroupMessageDTO dto);

    void recallMessage(Long id);

    /**
     * 拉取离线消息，只能拉取最近1个月的消息，最多拉取1000条
     *
     * @param minId 消息起始id
     */
    void  pullOfflineMessage(Long minId);

    void readedMessage(Long groupId);

    /**
     * 查询群里消息已读用户id列表
     * @param groupId 群里id
     * @param messageId 消息id
     * @return 已读用户id集合
     */
    List<Long> findReadedUsers(Long groupId,Long messageId);

    List<GroupMessageVO> findHistoryMessage(Long groupId, Long page, Long size);
}
