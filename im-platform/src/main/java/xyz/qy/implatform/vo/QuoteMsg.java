package xyz.qy.implatform.vo;

import lombok.Data;

import java.util.List;

@Data
public class QuoteMsg {
    private Long id;

    private String content;

    private Long sendId;

    private Integer status;

    private Integer type;

    private List<Long> atUserIds;

    private String sendAvatar;

    private String sendNickName;

    private Boolean isTemplate;
}
