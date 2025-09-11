package xyz.qy.implatform.dto;

import lombok.Data;
import xyz.qy.implatform.vo.InviteFriendVO;

import java.util.List;

/**
 * 进入群聊的用户信息
 *
 * @author Polaris
 * @since 2025-09-11
 */
@Data
public class EnterGroupUsersDTO {
    private Long groupId;

    private Long launchUserId;

    private List<InviteFriendVO> reviewUserList;

    private List<InviteFriendVO> noReviewUserList;
}
