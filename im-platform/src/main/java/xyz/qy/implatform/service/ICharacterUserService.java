package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.dto.CharacterUserBindDTO;
import xyz.qy.implatform.entity.CharacterUser;
import xyz.qy.implatform.vo.CharacterUserVO;

import java.util.List;

public interface ICharacterUserService extends IService<CharacterUser> {

    List<Long> getUserIdListByCharacterIds(List<Long> characterIds);

    List<CharacterUserVO> getCharacterUsersByCharacterId(Long characterId);

    void bindCharacterUser(CharacterUserBindDTO dto);

    void unbindCharacterUser(CharacterUserBindDTO dto);
}
