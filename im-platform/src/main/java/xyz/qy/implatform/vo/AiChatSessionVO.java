package xyz.qy.implatform.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AiChatSessionVO {
    private Long id;

    private Long userId;

    private String title;

    private Boolean deleted;

    private Date createTime;

    private List<AiChatMessageVO> messages;
}
