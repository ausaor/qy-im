package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.CharacterEmo;

import java.util.List;

public interface ICharacterEmoService extends IService<CharacterEmo> {
    /**
     * 查询已发布状态的角色表情包
     *
     * @param characterId 角色id
     * @return 角色表情包
     */
    JSONObject findPublishedEmoByCharacterId(Long characterId);
}
