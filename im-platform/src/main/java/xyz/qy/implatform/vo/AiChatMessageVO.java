package xyz.qy.implatform.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AiChatMessageVO {
    private Long id;

    private Long sessionId;
    private Long userId;
    private String role;
    private String model;

    private String content;

    private Date createTime;
}
