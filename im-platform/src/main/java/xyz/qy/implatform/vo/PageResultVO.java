package xyz.qy.implatform.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @description: 分页结果对象
 * @author: Polaris
 * @create: 2023-09-03 10:00
 **/
@Data
@Builder
public class PageResultVO<T> {
    /**
     * 结果数据
     */
    private T data;

    /**
     * 总数
     */
    private long total;
}
