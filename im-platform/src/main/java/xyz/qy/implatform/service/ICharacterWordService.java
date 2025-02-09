package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.CharacterWordDTO;
import xyz.qy.implatform.dto.CharacterWordSaveDTO;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.vo.CharacterWordVO;

import java.util.List;

public interface ICharacterWordService extends IService<CharacterWord> {
    /**
     * 查询已发布状态的角色台词
     *
     * @param characterId 角色id
     * @return 角色台词
     */
    JSONObject findPublishedWordByCharacterId(Long characterId);

    /**
     * 查询群聊模板角色台词
     *
     * @param dto 入参
     * @return 角色台词
     */
    List<CharacterWordVO> findCharacterWords(CharacterWordDTO dto);

    /**
     * 保存台词
     *
     * @param dto 入参
     */
    void save(CharacterWordSaveDTO dto);

    /**
     * 删除台词
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 发布台词
     *
     * @param dto 入参
     */
    void publish(CharacterWordDTO dto);
}
