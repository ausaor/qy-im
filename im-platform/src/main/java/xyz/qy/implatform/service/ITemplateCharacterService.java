package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.vo.SelectableTemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import java.util.List;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 16:34
 **/
public interface ITemplateCharacterService extends IService<TemplateCharacter> {

    List<TemplateCharacterVO> findTemplateCharactersByGroupId(Long templateGroupId);

    /**
     * 查询模板群聊可选择的模板人物
     *
     * @param vo 入参
     * @return 模板群聊下可选择的模板人物
     */
    List<TemplateCharacterVO> findSelectableTemplateCharacter(SelectableTemplateCharacterVO vo);

    /**
     * 根据模板角色id查询模板角色
     *
     * @param id 模板角色id
     * @return 模板角色
     */
    TemplateCharacterVO findByCharacterId(Long id);

    /**
     * 新增或修改模板人物信息
     *
     * @param templateGroupVO 模板群聊VO
     */
    void addOrModifyTemplateCharacters(TemplateGroupVO templateGroupVO);

    /**
     * 删除模板人物
     *
     * @param id 模板人物id
     */
    void delete(Long id);

    /**
     * 查询用户创建的模板群聊的模板人物数量
     *
     * @return 数量
     */
    int countUserTemplateCharacter(Long templateGroupId);

    /**
     * 查询已发布的模板角色
     *
     * @param templateGroupId 群聊模板id
     * @return 已发布的模板角色
     */
    List<TemplateCharacterVO> findPublishedByGroupId(Long templateGroupId);

    /**
     * 根据群聊模板id查询模板角色id
     *
     * @param templateGroupId 群聊模板id
     * @return 模板角色id
     */
    List<Long> findPublishedCharacterIdsByGroupId(Long templateGroupId);

    /**
     * 查询已发布的模板角色
     *
     * @param id id
     * @return 模板角色
     */
    TemplateCharacter findPublishedById(Long id);

    /**
     * 根据群聊模板id查询已发布的模板角色
     *
     * @param templateGroupId 群聊模板id
     * @return 已发布的模板角色
     */
    List<TemplateCharacter> findPublishedByTemplateGroupIdAndCharacterIds(Long templateGroupId, List<Long> characterIds);

    /**
     * 根据模板角色id查询已发布的模板角色
     *
     * @param characterIds 角色id
     * @return 模板角色list
     */
    List<TemplateCharacter> findPublishedByCharacterIds(List<Long> characterIds);

    /**
     * 获取指定数量的模板角色
     *
     * @param count 数量
     * @return 模板角色
     */
    List<TemplateCharacterVO> getRandomCharacters(Integer count);
}
