package xyz.qy.implatform.vo;

import lombok.Data;

@Data
public class QuoteMsg {
    private Long id;

    private String content;

    private Long sendId;

    private Integer status;

    private Integer type;
}
