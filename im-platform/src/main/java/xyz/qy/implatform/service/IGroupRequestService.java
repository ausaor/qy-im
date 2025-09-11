package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EnterGroupUsersDTO;
import xyz.qy.implatform.entity.GroupRequest;

/**
 * 群组请求表 服务类
 *
 * @author Polaris
 * @since 2025-09-11
 */
public interface IGroupRequestService extends IService<GroupRequest> {
    /**
     * 保存进群请求信息
     *
     * @param enterGroupUsersDTO 进群信息
     */
    void saveEnterGroupRequestInfo(EnterGroupUsersDTO enterGroupUsersDTO);
}
