package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.EnterGroupUsersDTO;
import xyz.qy.implatform.dto.GroupRequestUpdateDTO;
import xyz.qy.implatform.entity.GroupRequest;
import xyz.qy.implatform.vo.GroupJoinVO;
import xyz.qy.implatform.vo.GroupRequestVO;

import java.util.List;

/**
 * 群组请求表 服务类
 *
 * @author Polaris
 * @since 2025-09-11
 */
public interface IGroupRequestService extends IService<GroupRequest> {
    /**
     * 群组请求列表
     *
     * @return 群组请求列表
     */
    List<GroupRequestVO> groupRequestList();

    /**
     * 保存进群请求信息
     *
     * @param enterGroupUsersDTO 进群信息
     */
    void saveEnterGroupRequestInfo(EnterGroupUsersDTO enterGroupUsersDTO);

    /**
     * 保存用户进群请求信息
     *
     * @param groupJoinVO 进群信息
     */
    void saveUserJoinGroupRequestInfo(GroupJoinVO groupJoinVO);

    /**
     * 撤回请求
     *
     * @param id 请求id
     */
    void recall(Long id);

    /**
     * 拒绝请求
     *
     * @param id 请求id
     */
    void reject(Long id);

    /**
     * 同意请求
     *
     * @param id 请求id
     */
    void approve(Long id);

    /**
     * 更新请求
     *
     * @param dto 请求信息
     */
    void update(GroupRequestUpdateDTO dto);
}
