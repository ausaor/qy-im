package xyz.qy.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 地区Vo
 *
 * @author Polaris
 * @since 2024-10-26
 */
@Data
@ApiModel("地区VO")
public class RegionVO {
    @ApiModelProperty(value = "地区编码")
    private String code;

    /**
     * 上级地区编码
     */
    private String parentCode;

    /**
     * 地区级别
     */
    private Integer level;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 全称
     */
    private String fullName;

    /**
     * 地区领导称呼
     */
    private String leaderCallName;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
