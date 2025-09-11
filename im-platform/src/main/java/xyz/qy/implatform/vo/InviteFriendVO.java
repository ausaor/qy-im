package xyz.qy.implatform.vo;

import lombok.Data;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-03-26 09:15
 **/
@Data
public class InviteFriendVO {
    /**
     * 好友id
     */
    public Long friendId;

    /**
     * 模板人物id
     */
    public Long templateCharacterId;

    /**
     * 模板人物头像
     */
    public String templateCharacterAvatar;

    /**
     * 模板人物名称
     */
    public String templateCharacterName;
}
