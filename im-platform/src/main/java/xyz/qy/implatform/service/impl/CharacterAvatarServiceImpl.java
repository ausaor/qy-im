package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.enums.AuditResultEnum;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.CharacterAvatarMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.CharacterAvatarVO;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateCharacterVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description: 模板人物头像
 * @author: Polaris
 * @create: 2023-06-10 16:18
 **/
@Slf4j
@Service
public class CharacterAvatarServiceImpl extends ServiceImpl<CharacterAvatarMapper, CharacterAvatar> implements ICharacterAvatarService {
    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Override
    public List<CharacterAvatarVO> queryPublishedCharacterAvatarByCharacterId(Long templateCharacterId) {
        List<CharacterAvatar> characterAvatars = baseMapper.selectList(new LambdaQueryWrapper<CharacterAvatar>()
                .eq(CharacterAvatar::getTemplateCharacterId, templateCharacterId)
                .eq(CharacterAvatar::getDeleted, false)
                .eq(CharacterAvatar::getStatus, ReviewEnum.REVIEWED.getCode()));
        return BeanUtils.copyProperties(characterAvatars, CharacterAvatarVO.class);
    }

    @Override
    public CharacterAvatar findPublishedCharacterAvatarById(Long id) {
        return this.lambdaQuery().eq(CharacterAvatar::getId, id)
                .eq(CharacterAvatar::getDeleted, false)
                .eq(CharacterAvatar::getStatus, ReviewEnum.REVIEWED.getCode()).one();
    }

    @Override
    public List<CharacterAvatarVO> queryAllCharacterAvatarByCharacterId(Long templateCharacterId) {
        LambdaQueryWrapper<CharacterAvatar> queryWrapper = new LambdaQueryWrapper<CharacterAvatar>()
                .eq(CharacterAvatar::getTemplateCharacterId, templateCharacterId)
                .eq(CharacterAvatar::getDeleted, false)
                .orderByAsc(CharacterAvatar::getLevel);
        List<CharacterAvatar> characterAvatars = baseMapper.selectList(queryWrapper);
        return BeanUtils.copyProperties(characterAvatars, CharacterAvatarVO.class);
    }

    @Override
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();

        CharacterAvatar characterAvatar = baseMapper.selectById(id);
        if (ObjectUtil.isNull(characterAvatar)) {
            throw new GlobalException("当前人物头像不存在");
        }
        characterAvatar.setDeleted(Boolean.TRUE);
        characterAvatar.setUpdateBy(session.getUserId().toString());
        baseMapper.updateById(characterAvatar);
    }

    @Override
    public void addOrModifyCharacterAvatars(TemplateCharacterVO templateCharacterVO) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        Long templateCharacterId = templateCharacterVO.getId();

        TemplateCharacter templateCharacter = templateCharacterService.getById(templateCharacterId);
        if (ObjectUtil.isNull(templateCharacter) || templateCharacter.getDeleted()) {
            throw new GlobalException("当前模板人物已被删除");
        }
        if (!userId.toString().equals(templateCharacter.getCreateBy())) {
            throw new GlobalException("您不是当前模板人物的创建人");
        }
        if (!ReviewEnum.REVIEWED.getCode().equals(templateCharacter.getStatus())) {
            throw new GlobalException("当前模板人物需审核通过后才能配置人物头像");
        }

        List<CharacterAvatarVO> characterAvatarVOList = templateCharacterVO.getCharacterAvatarVOList();
        if (CollectionUtils.isEmpty(characterAvatarVOList)) {
            throw new GlobalException("人物头像为空");
        }
        if (characterAvatarVOList.size() > Constant.MAX_CHARACTER_AVATAR_NUM) {
            throw new GlobalException("每位模板人物最多配置" + Constant.MAX_CHARACTER_AVATAR_NUM + "个头像");
        }

        List<CharacterAvatar> characterAvatarList = BeanUtils.copyProperties(characterAvatarVOList, CharacterAvatar.class);

        characterAvatarList.forEach(item -> {
            item.setCreateBy(userId.toString());
            item.setUpdateBy(userId.toString());
            item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
            item.setTemplateCharacterId(templateCharacter.getId());
            item.setTemplateCharacterName(templateCharacter.getName());
        });
        this.saveOrUpdateBatch(characterAvatarList);
    }

    @Override
    public void submitForApproval(Long templateCharacterId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        TemplateCharacter templateCharacter = templateCharacterService.getById(templateCharacterId);
        if (ObjectUtil.isNull(templateCharacter) || templateCharacter.getDeleted()) {
            throw new GlobalException("当前模板人物不存在");
        }
        if (!userId.toString().equals(templateCharacter.getCreateBy())) {
            throw new GlobalException("您不是当前模板人物创建人");
        }
        if (!ReviewEnum.REVIEWED.getCode().equals(templateCharacter.getStatus())) {
            throw new GlobalException("当前模板人物未审核通过");
        }
        LambdaQueryWrapper<CharacterAvatar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharacterAvatar::getTemplateCharacterId, templateCharacterId);
        queryWrapper.eq(CharacterAvatar::getDeleted, false);
        List<CharacterAvatar> characterAvatars = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(characterAvatars)) {
            throw new GlobalException("当前模板人物未配置人物头像");
        }
        long reviewingCount = characterAvatars.stream()
                .filter(item -> ReviewEnum.REVIEWING.getCode().equals(item.getStatus()))
                .count();
        if (characterAvatars.size() == reviewingCount) {
            throw new GlobalException("正在审批中，请勿重复提交");
        }
        characterAvatars.forEach(item -> {
            item.setStatus(ReviewEnum.REVIEWING.getCode());
            item.setUpdateBy(userId.toString());
        });
        this.updateBatchById(characterAvatars);
    }

    @Override
    public void withdrawalOfApproval(Long templateCharacterId) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        TemplateCharacter templateCharacter = templateCharacterService.getById(templateCharacterId);
        if (ObjectUtil.isNull(templateCharacter) || templateCharacter.getDeleted()) {
            throw new GlobalException("当前模板人物不存在");
        }
        if (!userId.toString().equals(templateCharacter.getCreateBy())) {
            throw new GlobalException("您不是当前模板人物创建人");
        }
        LambdaQueryWrapper<CharacterAvatar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharacterAvatar::getTemplateCharacterId, templateCharacterId);
        queryWrapper.eq(CharacterAvatar::getDeleted, false);
        List<CharacterAvatar> characterAvatars = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(characterAvatars)) {
            throw new GlobalException("当前模板人物未配置人物头像");
        }
        characterAvatars.forEach(item -> {
            item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
            item.setUpdateBy(userId.toString());
        });
        this.updateBatchById(characterAvatars);
    }

    @Override
    public void submitAuditConclusion(ReviewVO reviewVO) {
        UserSession session = SessionContext.getSession();
        Long userid = session.getUserId();
        Long templateCharacterId = reviewVO.getTemplateCharacterId();
        TemplateCharacter templateCharacter = templateCharacterService.getById(templateCharacterId);
        if (ObjectUtil.isNull(templateCharacter) || templateCharacter.getDeleted()) {
            throw new GlobalException("当前模板人物不存在");
        }
        if (!ReviewEnum.REVIEWED.getCode().equals(templateCharacter.getStatus())) {
            throw new GlobalException("当前模板人物未审核通过");
        }
        LambdaQueryWrapper<CharacterAvatar> queryWrapper = new LambdaQueryWrapper<CharacterAvatar>()
                .eq(CharacterAvatar::getTemplateCharacterId, templateCharacterId)
                .eq(CharacterAvatar::getDeleted, false);
        List<CharacterAvatar> characterAvatars = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(characterAvatars)) {
            throw new GlobalException("当前模板人物未配置人物头像");
        }
        String status = AuditResultEnum.PASS.getCode().equals(reviewVO.getComments())
                ? ReviewEnum.REVIEWED.getCode() : ReviewEnum.NO_PASS.getCode();
        characterAvatars.forEach(item -> {
            item.setStatus(status);
            item.setUpdateBy(userid.toString());
        });
        this.updateBatchById(characterAvatars);
    }

    @Override
    public List<TemplateCharacterVO> findReviewingCharacterAvatar() {
        LambdaQueryWrapper<CharacterAvatar> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CharacterAvatar::getStatus, ReviewEnum.REVIEWING.getCode());
        queryWrapper.eq(CharacterAvatar::getDeleted, false);
        List<CharacterAvatar> characterAvatarList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(characterAvatarList)) {
            return Collections.emptyList();
        }
        List<CharacterAvatarVO> characterAvatarVOS = BeanUtils.copyProperties(characterAvatarList, CharacterAvatarVO.class);
        Map<Long, List<CharacterAvatarVO>> avatarMap = characterAvatarVOS.stream().collect(
                Collectors.groupingBy(CharacterAvatarVO::getTemplateCharacterId));

        Set<Long> templateCharacterIds = avatarMap.keySet();
        List<TemplateCharacter> templateCharacters = templateCharacterService.listByIds(templateCharacterIds);
        if (CollectionUtils.isEmpty(templateCharacters)) {
            throw new GlobalException("数据异常");
        }
        List<TemplateCharacterVO> templateCharacterVOS = BeanUtils.copyProperties(templateCharacters, TemplateCharacterVO.class);
        for (TemplateCharacterVO templateCharacterVO : templateCharacterVOS) {
            if (avatarMap.containsKey(templateCharacterVO.getId())) {
                templateCharacterVO.setCharacterAvatarVOList(avatarMap.get(templateCharacterVO.getId()));
            }
        }
        return templateCharacterVOS;
    }
}
