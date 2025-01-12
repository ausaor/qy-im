package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.mapper.CharacterWordMapper;
import xyz.qy.implatform.service.ICharacterWordService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterWordVO;

import java.util.List;

@Slf4j
@Service
public class CharacterWordServiceImpl extends ServiceImpl<CharacterWordMapper, CharacterWord> implements ICharacterWordService {
    @Override
    public List<CharacterWordVO> findPublishedWordByCharacterId(Long characterId) {
        List<CharacterWord> list = this.lambdaQuery().eq(CharacterWord::getCharacterId, characterId)
                .eq(CharacterWord::getStatus, ReviewEnum.REVIEWED.getCode())
                .list();
        return BeanUtils.copyProperties(list, CharacterWordVO.class);
    }
}
