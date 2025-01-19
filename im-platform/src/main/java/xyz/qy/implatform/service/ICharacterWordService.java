package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
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
}
