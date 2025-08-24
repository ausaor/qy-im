package xyz.qy.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestVO {
    private Long id;

    /**
     * 发送者id
     */
    private Long sendId;

    /**
     * 发送者头像
     */
    private String sendHeadImage;

    /**
     * 发送者昵称
     */
    private String sendNickName;

    /**
     * 接收者id
     */
    private Long recvId;

    /**
     * 接收者头像
     */
    private String recvHeadImage;

    /**
     * 接收者昵称
     */
    private String recvNickName;

    /**
     * 状态（0：撤回；1：申请；2：同意；3：拒绝）
     */
    private Integer status;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 申请备注
     */
    private String remark;
}
