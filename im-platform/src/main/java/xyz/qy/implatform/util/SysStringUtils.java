package xyz.qy.implatform.util;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * @description: 字符串工具类
 * @author: Polaris
 * @create: 2023-06-05 21:04
 **/
@Slf4j
public class SysStringUtils {
    /**
     * 校验是否特殊字符
     */
    public static boolean checkSpecialChar(String str) {
        //* ? ! & ￥ $ % ^ # , . / @ " ; : > < ] [ } { - = + _ \ | 》 《 。 ， 、 ？ ’ ‘ “ ” ~ `（）
        String pattern = ".*[*?!&￥$%^#,./@\";:><\\]\\[}{\\-=+\\\\|》《。，、？’‘“”~`（）].*$";
        return Pattern.matches(pattern, str);
    }

    /**
     * 生成随机6位密码
     *
     * @return 密码
     */
    public static String makeRandomPassword() {
        return RandomUtil.randomString(6);
    }
}
