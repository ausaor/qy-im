package xyz.qy.implatform.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorUtil {

    private final Snowflake snowflake;

    public IdGeneratorUtil() {
        // 可以根据需要配置workerId和datacenterId
        this.snowflake = IdUtil.getSnowflake(1, 1);
    }

    /**
     * 生成Long类型ID
     */
    public long nextId() {
        return snowflake.nextId();
    }

    /**
     * 生成String类型ID
     */
    public String nextIdStr() {
        return snowflake.nextIdStr();
    }

    /**
     * 批量生成ID
     */
    public long[] nextIds(int count) {
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = snowflake.nextId();
        }
        return ids;
    }
}
