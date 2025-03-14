package xyz.qy.implatform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地区群聊各级别群主投票生效所需票数
 */
@Getter
@AllArgsConstructor
public enum LeaderVoteNumEnum {

    LEVEL_1(1, 200),

    LEVEL_2(2, 160),

    LEVEL_3(3, 120),

    LEVEL_4(4, 80),

    LEVEL_5(5, 40),

    LEVEL_6(6, 20),
    ;

    private final Integer level;

    private final Integer count;

    public static Integer getCountByLevel(Integer level) {
        int count = 20;
        for (LeaderVoteNumEnum item : LeaderVoteNumEnum.values()) {
            if (item.getLevel().equals(level)) {
                return item.getCount();
            }
        }
        return count;
    }
}
