package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Group;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.CommonGroupVO;
import xyz.qy.implatform.vo.GroupInviteVO;
import xyz.qy.implatform.vo.GroupJoinVO;
import xyz.qy.implatform.vo.GroupMemberVO;
import xyz.qy.implatform.vo.GroupVO;
import xyz.qy.implatform.vo.MultCharacterGroupVO;
import xyz.qy.implatform.vo.PageResultVO;
import xyz.qy.implatform.vo.SwitchTemplateGroupVO;
import xyz.qy.implatform.vo.TemplateGroupCreateVO;

import java.util.List;


public interface IGroupService extends IService<Group> {


    GroupVO createGroup(GroupVO vo);

    GroupVO modifyGroup(GroupVO vo);

    void deleteGroup(Long groupId);

    void quitGroup(Long groupId);

    void kickGroup(Long groupId,Long userId);

    List<GroupVO>  findGroups();

    void invite(GroupInviteVO vo);

    Group GetById(Long groupId);

    /**
     * 根据id查找群聊，并进行缓存
     *
     * @param groupId 群聊id
     * @return 群聊实体
     */
    Group getAndCheckById(Long groupId);

    GroupVO findById(Long groupId);

    List<GroupMemberVO> findGroupMembers(Long groupId);

    GroupVO createTemplateGroup(TemplateGroupCreateVO templateGroupCreateVO);

    GroupVO switchTemplateGroup(SwitchTemplateGroupVO vo);

    GroupVO switchCommonGroup(CommonGroupVO vo);

    GroupVO switchMultCharacterGroup(MultCharacterGroupVO vo);

    GroupVO switchMoreCharactersGroup(MultCharacterGroupVO vo);

    /**
     * 将用户加入公共群聊
     *
     * @param user 用户
     */
    GroupMember addToCommonGroup(User user, Long groupId);

    /**
     * 查询当前用户未加入的群聊
     *
     * @param keyWord 搜索关键词
     * @return 群聊
     */
    PageResultVO queryNotJoinGroups(String keyWord);

    /**
     * 请求加入群聊
     *
     * @param vo 群聊信息vo
     */
    GroupVO joinGroup(GroupJoinVO vo);
}
