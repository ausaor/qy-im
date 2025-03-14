package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.RegionGroupMessageDTO;
import xyz.qy.implatform.entity.RegionGroupMessage;
import xyz.qy.implatform.vo.RegionGroupMessageVO;

import java.util.List;

/**
 * 地区群聊消息
 *
 * @author Polaris
 * @since 2022-11-03
 */
public interface IRegionGroupMessageService extends IService<RegionGroupMessage> {
    RegionGroupMessageVO sendMessage(RegionGroupMessageDTO dto);

    void pullOfflineMessage(Long minId);

    void readedMessage(Long regionGroupId);

    /**
     * 查询群里消息已读用户id列表
     *
     * @param regionGroupId 地区群聊id
     * @param messageId 消息id
     * @return 已读用户id集合
     */
    List<Long> findReadedUsers(Long regionGroupId,Long messageId);

    /**
     * 撤回消息
     *
     * @param id 消息id
     */
    void recallMessage(Long id);

    /**
     * 查询地区群聊历史记录
     *
     * @param regionGroupId 地区群聊id
     * @param page 当前页
     * @param size 每页大小
     * @return 地区群聊历史记录
     */
    List<RegionGroupMessageVO> findHistoryMessage(Long regionGroupId, Long page, Long size);
}
