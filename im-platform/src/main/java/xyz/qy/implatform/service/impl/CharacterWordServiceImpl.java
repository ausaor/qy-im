package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.CharacterWordDTO;
import xyz.qy.implatform.dto.CharacterWordSaveDTO;
import xyz.qy.implatform.entity.CharacterWord;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.CharacterWordMapper;
import xyz.qy.implatform.service.ICharacterUserService;
import xyz.qy.implatform.service.ICharacterWordService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterWordVO;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CharacterWordServiceImpl extends ServiceImpl<CharacterWordMapper, CharacterWord> implements ICharacterWordService {
    @Resource
    private ITemplateCharacterService characterService;

    @Resource
    private ITemplateGroupService templateGroupService;

    @Resource
    private ITemplateCharacterService templateCharacterService;

    @Resource
    private ICharacterUserService characterUserService;
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

    @Override
    public List<CharacterWordVO> findCharacterWords(CharacterWordDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<CharacterWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharacterWord::getTemplateGroupId, dto.getTemplateGroupId());
        if (ObjectUtil.isNull(dto.getCharacterId())) {
            queryWrapper.eq(CharacterWord::getCharacterId, -1);
        } else {
            queryWrapper.eq(CharacterWord::getCharacterId, dto.getCharacterId());
        }
        queryWrapper.eq(CharacterWord::getDeleted, Constant.NO);

        List<CharacterWord> characterWords = this.list(queryWrapper);
        List<CharacterWordVO> characterWordVOS = BeanUtils.copyPropertiesList(characterWords, CharacterWordVO.class);
        characterWordVOS.forEach(item -> {
            if (userId.equals(item.getCreateBy())) {
                item.setIsOwner(true);
            }
        });

        return characterWordVOS;
    }

    @Transactional
    @Override
    public void save(CharacterWordSaveDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        if (CollectionUtils.isEmpty(dto.getWords())) {
            throw new GlobalException("台词不能为空");
        }
        if (dto.getWords().size() > 50) {
            throw new GlobalException("单个角色语音台词数量不能超过50");
        }

        // 判断群聊模板是否存在
        TemplateGroup templateGroup = templateGroupService.getById(dto.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || templateGroup.getDeleted()) {
            throw new GlobalException("群聊模板不存在");
        }

        boolean hasAuth = false;

        if (userId.toString().equals(templateGroup.getCreateBy())) {
            hasAuth = true;
        }

        String characterName;
        // 判断角色是否存在
        if (ObjectUtil.isNotNull(dto.getCharacterId())) {
            TemplateCharacter templateCharacter = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, dto.getTemplateGroupId())
                    .eq(TemplateCharacter::getId, dto.getCharacterId())
                    .eq(TemplateCharacter::getDeleted, false)
                    .one();
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("角色不存在");
            }

            List<Long> myCharacterIds = characterUserService.getMyCharacterIds();

            if (userId.toString().equals(templateCharacter.getCreateBy()) || myCharacterIds.contains(dto.getCharacterId())) {
                hasAuth = true;
            }
            characterName = templateCharacter.getName();
        } else {
            characterName = templateGroup.getGroupName();
        }

        if (!hasAuth) {
            throw new GlobalException("您没有权限");
        }

        Long characterId = ObjectUtil.isNotNull(dto.getCharacterId()) ? dto.getCharacterId() : -1;

        List<CharacterWordDTO> wordDTOList = dto.getWords().stream().filter(item -> StringUtils.isNotBlank(item.getWord())).collect(Collectors.toList());

        List<CharacterWord> characterWords = BeanUtils.copyPropertiesList(wordDTOList, CharacterWord.class);

        characterWords.forEach(item -> {
            item.setCharacterName(characterName);
            item.setCharacterId(characterId);
            item.setTemplateGroupId(dto.getTemplateGroupId());
            item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
            item.setCreateBy(userId);
            item.setUpdateBy(userId);
        });

        this.saveOrUpdateBatch(characterWords);
    }

    @Override
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        CharacterWord characterWord = this.getById(id);
        if (!userId.equals(characterWord.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        characterWord.setDeleted(true);
        this.updateById(characterWord);
    }

    @Override
    public void publish(CharacterWordDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        // 判断群聊模板是否存在
        TemplateGroup templateGroup = templateGroupService.getById(dto.getTemplateGroupId());
        if (ObjectUtil.isNull(templateGroup) || templateGroup.getDeleted()) {
            throw new GlobalException("群聊模板不存在");
        }

        boolean hasAuth = false;
        if (userId.toString().equals(templateGroup.getCreateBy())) {
            hasAuth = true;
        }

        // 判断角色是否存在
        if (ObjectUtil.isNotNull(dto.getCharacterId())) {

            TemplateCharacter templateCharacter = templateCharacterService.lambdaQuery()
                    .eq(TemplateCharacter::getTemplateGroupId, dto.getTemplateGroupId())
                    .eq(TemplateCharacter::getId, dto.getCharacterId())
                    .eq(TemplateCharacter::getDeleted, false)
                    .one();
            if (ObjectUtil.isNull(templateCharacter)) {
                throw new GlobalException("角色不存在");
            }

            List<Long> myCharacterIds = characterUserService.getMyCharacterIds();

            if (userId.toString().equals(templateCharacter.getCreateBy()) || myCharacterIds.contains(dto.getCharacterId())) {
                hasAuth = true;
            }
        }

        if (!hasAuth) {
            throw new GlobalException("您没有权限");
        }

        Long characterId = ObjectUtil.isNotNull(dto.getCharacterId()) ? dto.getCharacterId() : -1;

        Date now = new Date();
        LambdaUpdateWrapper<CharacterWord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(CharacterWord::getStatus, ReviewEnum.REVIEWED.getCode());
        updateWrapper.set(CharacterWord::getUpdateTime, now);
        updateWrapper.set(CharacterWord::getUpdateBy, userId);
        updateWrapper.eq(CharacterWord::getTemplateGroupId, dto.getTemplateGroupId());
        updateWrapper.eq(CharacterWord::getCharacterId, characterId);
        updateWrapper.eq(CharacterWord::getDeleted, false);

        this.update(updateWrapper);
    }
}
