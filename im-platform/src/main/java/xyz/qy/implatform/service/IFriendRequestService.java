package xyz.qy.implatform.service;


import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.FriendRequest;
import xyz.qy.implatform.vo.FriendRequestVO;

import java.util.List;

/**
 * 请求添加好友表 服务类
 *
 * @author Polaris
 * @since 2025-08-24
 */
public interface IFriendRequestService extends IService<FriendRequest> {
    /**
     * 获取好友请求列表
     *
     * @return
     */
    List<FriendRequestVO> friendRequestList();

    /**
     * 同意好友请求
     *
     * @param id 请求id
     */
    void approve(Long id);

    /**
     * 拒绝好友请求
     *
     * @param id 请求id
     */
    void reject(Long id);

    /**
     * 撤回好友请求
     *
     * @param id 请求id
     */
    void recall(Long id);
}
