package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class GroupRequestVO {
    /**
     * 请求id
     */
    private Long id;

    /**
     * 群id
     */
    private Long groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 群头像
     */
    private String groupHeadImage;

    /**
     * 群聊类型
     */
    private Integer groupType;

    /**
     * 群主id
     */
    private Long groupOwnerId;

    /**
     * 类型（1：用户主动申请加入；2：用户被邀请加入）
     */
    private Integer type;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userHeadImage;

    /**
     * 状态（0：撤回；1：审核中；2：同意；3：拒绝）
     */
    private Integer status;

    /**
     * 发起人id
     */
    private Long launchUserId;

    /**
     * 模板角色id
     */
    private Long templateCharacterId;

    /**
     * 模板角色名称
     */
    private String templateCharacterName;

    /**
     * 模板角色头像
     */
    private String templateCharacterAvatar;

    /**
     * 备注
     */
    private String remark;
}
