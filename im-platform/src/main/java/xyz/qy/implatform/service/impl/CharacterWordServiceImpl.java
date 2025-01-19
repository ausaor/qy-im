package xyz.qy.implatform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.mapper.CharacterWordMapper;
import xyz.qy.implatform.service.ICharacterWordService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterWordVO;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class CharacterWordServiceImpl extends ServiceImpl<CharacterWordMapper, CharacterWord> implements ICharacterWordService {
    @Resource
    private ITemplateCharacterService characterService;
    @Override
    public JSONObject findPublishedWordByCharacterId(Long characterId) {
        TemplateCharacter character = characterService.getById(characterId);

        JSONObject jsonObject = new JSONObject();
        List<CharacterWord> characterWords = this.lambdaQuery()
                .eq(CharacterWord::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterWord::getCharacterId, characterId)
                .eq(CharacterWord::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        List<CharacterWordVO> characterWordVOS = BeanUtils.copyProperties(characterWords, CharacterWordVO.class);

        List<CharacterWord> groupWords = this.lambdaQuery()
                .eq(CharacterWord::getTemplateGroupId, character.getTemplateGroupId())
                .eq(CharacterWord::getCharacterId, -1)
                .eq(CharacterWord::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();

        List<CharacterWordVO> groupWordVOS = BeanUtils.copyProperties(groupWords, CharacterWordVO.class);

        jsonObject.put("group", groupWordVOS);
        jsonObject.put("character", characterWordVOS);
        return jsonObject;
    }
}
