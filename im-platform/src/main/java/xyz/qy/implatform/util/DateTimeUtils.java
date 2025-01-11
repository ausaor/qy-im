package xyz.qy.implatform.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理工具类
 *
 * @author Polaris
 * @version 1.0
 */
public final class DateTimeUtils extends DateUtils {

    private DateTimeUtils() {
    }

    public static final String FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PARTDATEFORMAT = "yyyyMMdd";


    /**
     * 将日期类型转换为字符串
     *
     * @param date    日期
     * @param xFormat 格式
     * @return 日期
     */
    public static String getFormatDate(Date date, String xFormat) {
        date = date == null ? new Date() : date;
        xFormat = StringUtils.isNotEmpty(xFormat) ? xFormat : FULL_DATE_FORMAT;
        SimpleDateFormat sdf = new SimpleDateFormat(xFormat);
        return sdf.format(date);
    }

    /**
     * 根据时间秒数获取中文时间描述
     *
     * @param value 秒数
     * @return 时间描述：xx天xx小时xx分xx秒
     */
    public static String getTimeValueDesc(long value) {
        StringBuilder builder = new StringBuilder();
        // 1、计算单独剩余的秒
        if (value > 60) {
            // 完整分钟数
            long minuteNum = value / 60;
            // 单独剩余的秒
            long seconds = value - minuteNum * 60;
            builder.append(seconds).append("秒");

            if (minuteNum > 60) {
                // 完整的小时
                long hourNum = minuteNum / 60;
                // 单独剩余的分钟
                long minutes = minuteNum - hourNum * 60;
                builder.insert(0, "分").insert(0, minutes);

                if (hourNum > 24) {
                    // 完整的天数
                    long dayNum = hourNum / 24;
                    // 单独剩余的小时
                    long hours = hourNum - dayNum * 24;
                    builder.insert(0, "小时").insert(0, hours);

                    builder.insert(0, "天").insert(0, dayNum);
                } else {
                    builder.insert(0, "小时").insert(0, hourNum);
                }
            } else {
                builder.insert(0, "分").insert(0, minuteNum);
            }
        } else {
            builder.append(value).append("秒");
        }

        return builder.toString();
    }
}
