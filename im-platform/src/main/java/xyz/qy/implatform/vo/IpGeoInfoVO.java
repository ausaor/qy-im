package xyz.qy.implatform.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description: IP属地信息
 * @author: Polaris
 * @create: 2022-09-12 22:19
 **/
@Data
@Accessors(chain = true)
public class IpGeoInfoVO {
    /**
     * IP
     */
    private String ip;

    /**
     * 国家
     */
    private String nation;

    /**
     * 省
     */
    private String pro;

    /**
     * 省编码
     */
    private String proCode;

    /**
     * 市
     */
    private String city;

    /**
     * 市编码
     */
    private String cityCode;

    /**
     * IP地址信息
     */
    private String addr;

    /**
     * 位置信息
     */
    private String locationInfo;
}
