package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.SwitchCharacterAvatarVO;

import java.util.List;


public interface IGroupMemberService extends IService<GroupMember> {
    GroupMember findByGroupAndUserId(Long groupId,Long userId);

    List<GroupMember>  findByUserId(Long userId);

    /**
     * 根据用户id查询一个月内退的群
     *
     * @param userId 用户id
     * @return 成员列表
     */
    List<GroupMember> findQuitInMonth(Long userId);

    List<GroupMember>  findByGroupId(Long groupId);

    List<GroupMember> findNoQuitGroupMembers(Long groupId);

    List<Long> findUserIdsByGroupId(Long groupId);

    List<Long> getAllGroupIdsByUserId(Long userId);

    List<Long> getAllGroupMemberIdsByUserId(Long userId);

    boolean save(GroupMember member);

    boolean saveOrUpdateBatch(Long groupId,List<GroupMember> members);

    void removeByGroupId(Long groupId);

    void removeByGroupAndUserId(Long groupId,Long userId);

    void switchTemplateCharacter(GroupMemberVO groupMemberVO);

    void switchCharacterAvatar(SwitchCharacterAvatarVO avatarVO);

    /**
     * 用户用户是否在群中
     *
     * @param groupId 群聊id
     * @param userIds  用户id
     */
    Boolean isInGroup(Long groupId,List<Long> userIds);
}
