package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.CharacterAvatar;
import xyz.qy.implatform.entity.CommentCharacter;
import xyz.qy.implatform.entity.TemplateCharacter;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.CommentCharacterMapper;
import xyz.qy.implatform.service.ICharacterAvatarService;
import xyz.qy.implatform.service.ICommentCharacterService;
import xyz.qy.implatform.service.ITemplateCharacterService;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class CommentCharacterServiceImpl extends ServiceImpl<CommentCharacterMapper, CommentCharacter> implements ICommentCharacterService {
    @Resource
    private ICharacterAvatarService characterAvatarService;

    @Resource
    private ITemplateCharacterService characterService;

    @Override
    public void saveCommentCharacter(Long userId, Long targetId, String targetType, Long characterId, Long avatarId) {
        CommentCharacter commentCharacter = new CommentCharacter();
        commentCharacter.setUserId(userId);
        commentCharacter.setTargetId(targetId);
        commentCharacter.setTargetType(targetType);
        commentCharacter.setCharacterId(characterId);
        commentCharacter.setAvatarId(avatarId);
        commentCharacter.setCreateTime(LocalDateTime.now());
        this.save(commentCharacter);
    }

    @Override
    public void updateCommentCharacter(CommentCharacter commentCharacter) {
        this.updateById(commentCharacter);
    }

    @Override
    public void verifyCommentCharacter(Long userId, Long targetId, String targetType, Long characterId, Long avatarId) {
        CommentCharacter commentCharacter = this.lambdaQuery()
                .eq(CommentCharacter::getUserId, userId)
                .eq(CommentCharacter::getTargetId, targetId)
                .eq(CommentCharacter::getTargetType, targetType)
                .one();
        if (commentCharacter != null) {
            // 自己选择过的角色与入参的角色不一致
            if (!commentCharacter.getCharacterId().equals(characterId)) {
                throw new GlobalException("只能选择使用过的角色");
            }
            if (avatarId != null && commentCharacter.getAvatarId() != null && !commentCharacter.getAvatarId().equals(avatarId)) {
                throw new GlobalException("只能选择使用过的角色头像");
            }
        }

        CommentCharacter commentCharacter2 = this.lambdaQuery()
                .eq(CommentCharacter::getTargetId, targetId)
                .eq(CommentCharacter::getTargetType, targetType)
                .eq(CommentCharacter::getCharacterId, characterId)
                .one();

        // 自己选择的角色与其他人选择的角色一样
        if (commentCharacter2 != null) {
            if (!commentCharacter2.getUserId().equals(userId)) {
                throw new GlobalException("所选角色已被其他人使用");
            }
        }

        // 判断所选角色是否存在
        TemplateCharacter templateCharacter = characterService.findPublishedById(characterId);
        if (templateCharacter == null) {
            throw new GlobalException("所选角色不存在");
        }

        // 判断头像是否属于所选角色
        if (avatarId != null) {
            CharacterAvatar characterAvatar = characterAvatarService.findPublishedCharacterAvatarById(avatarId);
            if (characterAvatar == null) {
                throw new GlobalException("所选角色头像不存在");
            }

            if (!characterAvatar.getTemplateCharacterId().equals(characterId)) {
                throw new GlobalException("所选角色头像不属于当前角色");
            }
        }

        if (commentCharacter == null) {
            this.saveCommentCharacter(userId, targetId, targetType, characterId, avatarId);
        } else if (avatarId != null && !avatarId.equals(commentCharacter.getAvatarId())) {
            commentCharacter.setAvatarId(avatarId);
            this.updateCommentCharacter(commentCharacter);
        }
    }
}
