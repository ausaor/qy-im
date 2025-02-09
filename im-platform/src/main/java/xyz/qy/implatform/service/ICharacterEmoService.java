package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.CharacterEmoDTO;
import xyz.qy.implatform.dto.CharacterEmoSaveDTO;
import xyz.qy.implatform.entity.CharacterEmo;
import xyz.qy.implatform.vo.CharacterEmoVO;

import java.util.List;

public interface ICharacterEmoService extends IService<CharacterEmo> {
    /**
     * 查询已发布状态的角色表情包
     *
     * @param characterId 角色id
     * @return 角色表情包
     */
    JSONObject findPublishedEmoByCharacterId(Long characterId);

    /**
     * 查询角色表情
     *
     * @param dto 入参
     * @return 角色表情
     */
    List<CharacterEmoVO> findCharacterEmos(CharacterEmoDTO dto);

    /**
     * 删除角色表情
     *
     * @param id 主键
     */
    void delete(Long id);

    /**
     * 保存角色表情
     *
     * @param dto 入参
     */
    void save(CharacterEmoSaveDTO dto);
}
