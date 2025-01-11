package xyz.qy.implatform.util;

import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.dto.GroupMessageDTO;
import xyz.qy.implatform.dto.PrivateMessageDTO;
import xyz.qy.implatform.entity.GroupMember;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.vo.GroupMessageVO;
import xyz.qy.implatform.vo.PrivateMessageVO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公共工具类
 */
public class CommonUtils {
    /**
     * 检测邮箱是否合法
     *
     * @param username 用户名
     * @return 合法状态
     */
    public static boolean checkEmail(String username) {
        String rule = "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
        //正则表达式的模式 编译正则表达式
        Pattern p = Pattern.compile(rule);
        //正则表达式的匹配器
        Matcher m = p.matcher(username);
        //进行正则匹配
        return m.matches();
    }

    /**
     * 获取括号内容
     *
     * @param str str
     * @return {@link String} 括号内容
     */
    public static String getBracketsContent(String str) {
        return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
    }

    /**
     * 生成6位随机验证码
     *
     * @return 验证码
     */
    public static String getRandomCode() {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }

    /**
     * 转换List
     *
     * @param obj   obj
     * @param clazz clazz
     * @return {@link List<T>}
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return result;
    }

    /**
     * 转换set
     *
     * @param obj   obj
     * @param clazz clazz
     * @return {@link Set<T>}
     */
    public static <T> Set<T> castSet(Object obj, Class<T> clazz) {
        Set<T> result = new HashSet<>();
        if (obj instanceof Set<?>) {
            for (Object o : (Set<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return result;
    }

    public static GroupMessageDTO buildGroupMessageVO(Long groupId, String content, Integer type) {
        GroupMessageDTO groupMessageVO = new GroupMessageDTO();
        groupMessageVO.setGroupId(groupId);
        groupMessageVO.setContent(content);
        groupMessageVO.setType(type);
        return groupMessageVO;
    }

    public static PrivateMessageDTO buildPrivateMessageVO(Long recvId, String content, Integer type) {
        PrivateMessageDTO privateMessageVO = new PrivateMessageDTO();
        privateMessageVO.setRecvId(recvId);
        privateMessageVO.setContent(content);
        privateMessageVO.setType(type);
        return privateMessageVO;
    }

    public static String buildWelcomeMessage(User user, GroupMember groupMember) {
        if (groupMember.getIsTemplate()) {
            return "@" + groupMember.getAliasName() + "（" +user.getNickName() + ") 欢迎加入群聊#emo拍手;";
        } else {
            return "@" + user.getNickName() + " 欢迎加入群聊#emo拍手;";
        }
    }
}
