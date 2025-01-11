package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.vo.CharacterAvatarVO;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;

import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-06-10 16:17
 **/
public interface ICharacterAvatarService extends IService<CharacterAvatar> {
    /**
     * 根据模板人物id查询已发布模板人物头像
     *
     * @param templateCharacterId 模板人物id
     * @return 模板人物头像
     */
    List<CharacterAvatarVO> queryPublishedCharacterAvatarByCharacterId(Long templateCharacterId);

    /**
     * 查询已发布角色头像
     *
     * @param id 角色头像id
     * @return 角色头像
     */
    CharacterAvatar findPublishedCharacterAvatarById(Long id);

    /**
     * 根据模板人物id查询所有模板人物头像
     *
     * @param templateCharacterId 模板人物id
     * @return 模板人物头像
     */
    List<CharacterAvatarVO> queryAllCharacterAvatarByCharacterId(Long templateCharacterId);

    /**
     * 删除模板人物头像
     *
     * @param id id
     */
    void delete(Long id);

    /**
     * 新增或修改模板人物头像
     *
     * @param templateCharacterVO 模板人物VO
     */
    void addOrModifyCharacterAvatars(TemplateCharacterVO templateCharacterVO);

    /**
     * 提交审核
     *
     * @param templateCharacterId 模板人物id
     */
    void submitForApproval(Long templateCharacterId);

    /**
     * 撤回审核
     *
     * @param templateCharacterId 模板人物id
     */
    void withdrawalOfApproval(Long templateCharacterId);

    /**
     * 提交审核结论
     *
     * @param reviewVO 审核结论信息
     */
    void submitAuditConclusion(ReviewVO reviewVO);

    /**
     * 查询审核中的人物头像
     */
    List<TemplateCharacterVO> findReviewingCharacterAvatar();
}
