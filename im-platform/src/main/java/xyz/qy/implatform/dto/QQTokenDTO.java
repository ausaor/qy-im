package xyz.qy.implatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * qq token信息
 *
 * @author Polaris
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QQTokenDTO {
    /**
     * openid
     */
    private String openid;

    /**
     * 客户端id
     */
    private String client_id;

}
