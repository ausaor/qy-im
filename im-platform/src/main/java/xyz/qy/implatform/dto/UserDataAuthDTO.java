package xyz.qy.implatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataAuthDTO {

    private String category;

    private Long userId;

    private Long friendId;

    private Long groupId;

    private String regionCode;

    private Boolean groupVisible = false;

    private Boolean regionVisible = false;

    private Integer scope;
}
