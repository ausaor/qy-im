package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.CommentCharacter;
import xyz.qy.implatform.vo.CommentCharacterVO;

public interface ICommentCharacterService extends IService<CommentCharacter> {
    /**
     * 保存用户评论角色
     *
     * @param userId 用户id
     * @param targetId 目标id
     * @param targetType 目标类型
     * @param characterId 角色id
     * @param avatarId 头像id
     */
    void saveCommentCharacter(Long userId, Long targetId, String targetType, Long characterId, Long avatarId);

    /**
     * 更新用户评论角色
     *
     * @param commentCharacter 评论角色
     */
    void updateCommentCharacter(CommentCharacter commentCharacter);

    /**
     * 验证用户评论角色
     *
     * @param userId 用户id
     * @param targetId 目标id
     * @param targetType 目标类型
     * @param characterId 角色id
     * @param avatarId 头像id
     */
    void verifyCommentCharacter(Long userId, Long targetId, String targetType, Long characterId, Long avatarId);

    /**
     * 获取用户评论角色
     *
     * @param targetId 目标id
     * @param targetType 目标类型
     * @return 评论角色
     */
    CommentCharacterVO getCommentCharacter(Long targetId, String targetType);
}
