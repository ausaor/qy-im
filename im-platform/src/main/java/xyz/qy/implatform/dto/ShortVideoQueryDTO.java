package xyz.qy.implatform.dto;

import lombok.Data;

@Data
public class ShortVideoQueryDTO {
    private Long id;

    private Long userId;

    private Long objectId;

    private String category;

    private Integer scope;

    private String title;

    private Integer status;
}
