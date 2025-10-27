package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.RegionGroupBanDTO;
import xyz.qy.implatform.dto.RegionGroupDTO;
import xyz.qy.implatform.dto.RegionGroupMemberDTO;
import xyz.qy.implatform.entity.RegionGroup;
import xyz.qy.implatform.vo.RegionGroupMemberVO;
import xyz.qy.implatform.vo.RegionGroupVO;

import java.util.List;

/**
 * 地区群聊service
 *
 * @author Polaris
 * @since 2024-10-26
 */
public interface IRegionGroupService extends IService<RegionGroup> {
    /**
     * 查询用户常驻的地区群聊
     *
     * @return 地区群聊列表
     */
    List<RegionGroupVO> findRegionGroups();

    RegionGroupVO findById(Long regionGroupId);

    /**
     * 查询地区群聊
     *
     * @param code 地区编码
     * @return 地区群聊列表
     */
    List<RegionGroupVO> findRegionGroupsByCode(String code);

    /**
     * 修改地区群聊成员信息
     *
     * @param dto 入参
     * @return 群成员信息
     */
    RegionGroupMemberVO modifyRegionGroupMember(RegionGroupMemberDTO dto);

    /**
     * 根据地区群聊id查询常驻成员
     *
     * @param regionGroupId 地区群聊id
     * @return 地区群聊常驻成员列表
     */
    List<RegionGroupMemberVO> findRegionGroupMembers(Long regionGroupId);

    /**
     * 加入地区群聊
     *
     * @param regionGroupDTO 入参
     * @return 地区群聊
     */
    RegionGroupVO joinRegionGroup(RegionGroupDTO regionGroupDTO);

    /**
     * 加入指定地区群聊
     *
     * @param regionGroupDTO 入参
     * @return 地区群聊
     */
    RegionGroupVO joinTargetRegionGroup(RegionGroupDTO regionGroupDTO);

    /**
     * 退出地区群聊
     *
     * @param regionGroupDTO 入参
     */
    void quitRegionGroup(RegionGroupDTO regionGroupDTO);

    /**
     * 地区群聊群主投票
     *
     * @param vo 入参
     */
    void voteRegionGroupLeader(RegionGroupMemberDTO vo);

    /**
     * 地区群聊解除群主身份投票
     *
     * @param vo 入参
     */
    void voteRemoveRegionGroupLeader(RegionGroupMemberDTO vo);

    /**
     * 地区群聊禁言
     *
     * @param dto 入参
     */
    void banMsg(RegionGroupBanDTO dto);

    /**
     * 地区群聊解除禁言
     *
     * @param dto 入参
     */
    void unBanMsg(RegionGroupBanDTO dto);

    /**
     * 地区群聊群主转移
     *
     * @param dto 入参
     */
    void leaderTransfer(RegionGroupMemberDTO dto);

    /**
     * 判断用户是否成为某个地区群聊群主
     *
     * @param userId 用户id
     */
    boolean hasBecomeRegionGroupLeader(Long userId);
}
