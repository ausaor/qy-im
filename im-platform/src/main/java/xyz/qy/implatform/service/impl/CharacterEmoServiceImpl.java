package xyz.qy.implatform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.CharacterEmo;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.mapper.CharacterEmoMapper;
import xyz.qy.implatform.service.ICharacterEmoService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterEmoVO;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CharacterEmoServiceImpl extends ServiceImpl<CharacterEmoMapper, CharacterEmo> implements ICharacterEmoService {
    @Resource
    private ITemplateCharacterService characterService;

    @Override
    public JSONObject findPublishedEmoByCharacterId(Long characterId) {
        JSONObject jsonObject = new JSONObject();

        TemplateCharacter character = characterService.getById(characterId);

        List<CharacterEmo> groupEmos = this.lambdaQuery()
                .eq(CharacterEmo::getCharacterId, -1)
                .eq(CharacterEmo::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterEmo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<CharacterEmoVO> groupEmoVOS = BeanUtils.copyProperties(groupEmos, CharacterEmoVO.class);

        List<CharacterEmo> characterEmos = this.lambdaQuery()
                .eq(CharacterEmo::getCharacterId, characterId)
                .eq(CharacterEmo::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterEmo::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<CharacterEmoVO> characterEmoVOS = BeanUtils.copyProperties(characterEmos, CharacterEmoVO.class);
        jsonObject.put("group", groupEmoVOS);
        jsonObject.put("character", characterEmoVOS);

        return jsonObject;
    }
}
