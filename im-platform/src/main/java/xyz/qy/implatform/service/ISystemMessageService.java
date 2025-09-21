package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.SysMsgDelDTO;
import xyz.qy.implatform.dto.SysMsgQueryDTO;
import xyz.qy.implatform.dto.SystemMessageDTO;
import xyz.qy.implatform.entity.SystemMessage;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.SystemMessageVO;

public interface ISystemMessageService extends IService<SystemMessage> {
    /**
     * 根据id查询系统消息
     *
     * @param id 系统消息id
     * @return 系统消息
     */
    SystemMessageVO getBySysMsgId(Long id);

    /**
     * 保存系统消息
     *
     * @param dto 系统消息
     */
    void save(SystemMessageDTO dto);

    /**
     * 修改系统消息
     *
     * @param dto 系统消息
     */
    void modify(SystemMessageDTO dto);

    /**
     * 批量删除系统消息
     *
     * @param dto 系统消息
     */
    void batchDeleteByIds(SysMsgDelDTO dto);

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

    /**
     * 分页查询系统消息
     *
     * @param queryDTO 查询参数
     * @return 系统消息列表
     */
    PageResultVO pageSysMsg(SysMsgQueryDTO queryDTO);
}
