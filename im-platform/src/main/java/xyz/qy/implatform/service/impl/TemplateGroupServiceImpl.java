package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.entity.TemplateGroup;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.AuditResultEnum;
import xyz.qy.implatform.enums.ReviewEnum;
import xyz.qy.implatform.enums.RoleEnum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.TemplateGroupMapper;
import xyz.qy.implatform.service.ITemplateCharacterService;
import xyz.qy.implatform.service.ITemplateGroupService;
import xyz.qy.implatform.service.IUserService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.PageUtils;
import xyz.qy.implatform.vo.ReviewVO;
import xyz.qy.implatform.vo.TemplateGroupVO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-12 11:13
 **/
@Slf4j
@Service
public class TemplateGroupServiceImpl extends ServiceImpl<TemplateGroupMapper, TemplateGroup> implements ITemplateGroupService {
    @Autowired
    private ITemplateCharacterService templateCharacterService;

    @Autowired
    private IUserService userService;

    @Override
    public void addOrModify(TemplateGroupVO vo) {
        UserSession session = SessionContext.getSession();
        // 新增判断用户已创建的模板群聊数量是否大于10
        if (ObjectUtil.isNull(vo.getId()) && !session.getRole().equals(RoleEnum.SUPER_ADMIN.getCode())) {
            int count = this.countUserTemplateGroup();
            if (count >= Constant.USER_MAX_TEMPLATE_GROUP_NUM) {
                throw new GlobalException("每位用户最多只能创建" + Constant.USER_MAX_TEMPLATE_GROUP_NUM + "个模板群聊");
            }
        }
        Long userId = session.getUserId();
        if (StringUtils.isBlank(vo.getGroupName())) {
            throw new GlobalException("模板群聊名称为空");
        }
        if (StringUtils.isBlank(vo.getAvatar())) {
            throw new GlobalException("模板群聊头像为空");
        }

        TemplateGroup templateGroup = BeanUtils.copyProperties(vo, TemplateGroup.class);
        assert templateGroup != null;
        if (ObjectUtil.isNull(templateGroup.getId())) {
            templateGroup.setCreateBy(userId.toString());
        }
        templateGroup.setUpdateBy(userId.toString());
        templateGroup.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        this.saveOrUpdate(templateGroup);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("当前模板群聊已被删除");
        }
        templateGroup.setDeleted(true);
        templateGroup.setUpdateBy(userId.toString());
        baseMapper.updateById(templateGroup);

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateCharacters)) {
            return;
        }
        templateCharacters.forEach(item -> {
            item.setDeleted(true);
            item.setUpdateBy(userId.toString());
        });
        templateCharacterService.updateBatchById(templateCharacters);
    }

    @Override
    public List<TemplateGroupVO> findTemplateGroups(TemplateGroupVO templateGroupVO) {
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(templateGroupVO.getGroupName())) {
            queryWrapper.like(TemplateGroup::getGroupName, templateGroupVO.getGroupName());
        }
        if (ObjectUtil.isNotNull(templateGroupVO.getId())) {
            queryWrapper.eq(TemplateGroup::getId, templateGroupVO.getId());
        }
        queryWrapper.eq(TemplateGroup::getDeleted, false);
        queryWrapper.eq(TemplateGroup::getStatus, ReviewEnum.REVIEWED.getCode());
        List<TemplateGroup> templateGroupList = this.list(queryWrapper);

        return BeanUtils.copyProperties(templateGroupList, TemplateGroupVO.class);
    }

    @Override
    public List<TemplateGroupVO> findMyTemplateGroups() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getCreateBy, userId);
        queryWrapper.eq(TemplateGroup::getDeleted, false);
        List<TemplateGroup> templateGroupList = this.list(queryWrapper);
        List<TemplateGroupVO> templateGroupVOS = BeanUtils.copyProperties(templateGroupList, TemplateGroupVO.class);
        templateGroupVOS.forEach(item -> {
            item.setCreator(session.getUserName());
            item.setIsOwner(true);
        });
        return templateGroupVOS;
    }

    @Override
    public List<TemplateGroupVO> pageAllTemplateGroups() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getDeleted, false);
        Page<TemplateGroup> pageResult = baseMapper.selectPage(new Page<>(PageUtils.getPageNo(), PageUtils.getPageSize()), queryWrapper);
        if (CollectionUtils.isEmpty(pageResult.getRecords())) {
            return Collections.emptyList();
        }
        List<TemplateGroup> templateGroupList = pageResult.getRecords();
        List<String> userIds = templateGroupList.stream().map(TemplateGroup::getCreateBy)
                .distinct().collect(Collectors.toList());
        List<TemplateGroupVO> templateGroupVOS = BeanUtils.copyProperties(templateGroupList, TemplateGroupVO.class);
        List<User> userList = userService.listByIds(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));
        for (TemplateGroupVO templateGroupVO : templateGroupVOS) {
            if (userMap.containsKey(Long.parseLong(templateGroupVO.getCreateBy()))) {
                templateGroupVO.setCreator(userMap.get(Long.parseLong(templateGroupVO.getCreateBy()))
                        .getUserName());
            }
            if (String.valueOf(userId).equals(templateGroupVO.getCreateBy())) {
                templateGroupVO.setIsOwner(true);
            }
        }
        return templateGroupVOS;
    }

    @Override
    public int countUserTemplateGroup() {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getCreateBy, userId);
        queryWrapper.eq(TemplateGroup::getDeleted, false);
        return this.count(queryWrapper);
    }

    @Transactional
    @Override
    public void submitForApproval(Long id) {
        UserSession session = SessionContext.getSession();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("模板群聊不存在");
        }
        if (ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("数据已在审核中，请勿重复提交");
        }
        if (!session.getUserId().toString().equals(templateGroup.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        templateGroup.setStatus(ReviewEnum.REVIEWING.getCode());
        templateGroup.setUpdateBy(session.getUserId().toString());

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        queryWrapper.eq(TemplateCharacter::getDeleted, false);

        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateCharacters)) {
            throw new GlobalException("请为该模板群聊上传模板人物信息");
        }
        templateCharacters.forEach(item -> {
            item.setStatus(ReviewEnum.REVIEWING.getCode());
            item.setUpdateBy(session.getUserId().toString());
        });
        baseMapper.updateById(templateGroup);
        templateCharacterService.updateBatchById(templateCharacters);
    }

    @Transactional
    @Override
    public void withdrawalOfApproval(Long id) {
        UserSession session = SessionContext.getSession();
        TemplateGroup templateGroup = baseMapper.selectById(id);
        if (ObjectUtil.isNull(templateGroup)) {
            throw new GlobalException("模板群聊不存在");
        }
        if (!ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("当前数据不是审核中的状态");
        }
        if (!session.getUserId().toString().equals(templateGroup.getCreateBy())) {
            throw new GlobalException("您不是创建人");
        }
        templateGroup.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
        templateGroup.setUpdateBy(session.getUserId().toString());

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroup.getId());
        queryWrapper.eq(TemplateCharacter::getDeleted, false);

        baseMapper.updateById(templateGroup);
        List<TemplateCharacter> templateCharacters = templateCharacterService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(templateCharacters)) {
            templateCharacters.forEach(item -> {
                item.setStatus(ReviewEnum.TO_BE_REVIEW.getCode());
                item.setUpdateBy(session.getUserId().toString());
            });
            templateCharacterService.updateBatchById(templateCharacters);
        }
    }

    @Override
    public List<TemplateGroupVO> findReviewingTemplateGroups() {
        LambdaQueryWrapper<TemplateGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateGroup::getDeleted, false);
        queryWrapper.eq(TemplateGroup::getStatus, ReviewEnum.REVIEWING.getCode());
        List<TemplateGroup> templateGroups = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(templateGroups)) {
            return Collections.emptyList();
        }
        List<String> userIds = templateGroups.stream().map(TemplateGroup::getCreateBy).collect(Collectors.toList());

        List<User> userList = userService.listByIds(userIds);
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, Function.identity(), (key1, key2) -> key2));

        List<TemplateGroupVO> templateGroupVOS = BeanUtils.copyProperties(templateGroups, TemplateGroupVO.class);
        for (TemplateGroupVO templateGroupVO : templateGroupVOS) {
            if (userMap.containsKey(Long.parseLong(templateGroupVO.getCreateBy()))) {
                templateGroupVO.setCreator(userMap.get(Long.parseLong(templateGroupVO.getCreateBy()))
                        .getUserName());
            }
        }
        return templateGroupVOS;
    }

    @Transactional
    @Override
    public void submitAuditConclusion(ReviewVO reviewVO) {
        UserSession session = SessionContext.getSession();
        Long userid = session.getUserId();
        Long templateGroupId = reviewVO.getTemplateGroupId();
        TemplateGroup templateGroup = baseMapper.selectById(templateGroupId);
        if (!ReviewEnum.REVIEWING.getCode().equals(templateGroup.getStatus())) {
            throw new GlobalException("当前模板群聊状态不是审核中");
        }

        LambdaQueryWrapper<TemplateCharacter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateCharacter::getTemplateGroupId, templateGroupId);
        queryWrapper.eq(TemplateCharacter::getDeleted, false);
        List<TemplateCharacter> templateCharacterList = templateCharacterService.list(queryWrapper);

        String status = AuditResultEnum.PASS.getCode().equals(reviewVO.getComments())
                ? ReviewEnum.REVIEWED.getCode() : ReviewEnum.NO_PASS.getCode();
        templateGroup.setStatus(status);
        templateGroup.setUpdateBy(userid.toString());
        templateCharacterList.forEach(item -> {
            item.setStatus(status);
            item.setUpdateBy(userid.toString());
        });

        baseMapper.updateById(templateGroup);
        templateCharacterService.updateBatchById(templateCharacterList);
    }

    @Override
    public TemplateGroup findPublishedById(Long id) {
        return this.lambdaQuery().eq(TemplateGroup::getId, id)
                .eq(TemplateGroup::getStatus, ReviewEnum.REVIEWED.getCode())
                .eq(TemplateGroup::getDeleted, false)
                .one();
    }
}
