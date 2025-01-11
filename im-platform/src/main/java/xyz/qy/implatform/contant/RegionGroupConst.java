package xyz.qy.implatform.contant;

/**
 * 地区群聊相关常量
 *
 * @author Polaris
 * @since 2024-12-01
 */
public interface RegionGroupConst {
    /**
     * 投票过期时间（24小时）
     */
    int VOTE_EXPIRE_TIME = 24;

    /**
     * 投票生效所需票数（20）
     */
    int VOTE_EFFECT_NUM = 20;

    /**
     * 地区群聊群主有效期（30天）
     */
    int REGION_LEADER_PERIOD = 30;

    /**
     * 地区群聊群主有效最短时长（2天）
     */
    int MIN_LEADER_DURATION = 2;

    /**
     * 可以参与群主投票的最短加入群聊时长（7天）
     */
    int ENABLE_LEADER_TIME = 7;

    /**
     * 最多可加入的地区群聊数
     */
    int MAX_REGION_GROUP_NUM = 3;

    /**
     * 一个地区群聊常驻人数上限
     */
    int MAX_REGION_GROUP_MEMBER_COUNT = 400;

    /**
     * 一个地区群聊临时加入人数上限
     */
    int MAX_REGION_GROUP_TEMP_MEMBER_COUNT = 100;

    /**
     * 地区群聊加入间隔时长（30分钟）
     */
    int REGION_GROUP_JOIN_GAP = 30;

    /**
     * 临时用户加入地区群聊的有效时长（2小时）
     */
    int TEMP_MEMBER_DURATION = 2;


    int MSG_LIMIT_TIME = 24;

    int MSG_LIMIT_COUNT = 50;

    int LEADER_TRANSFER_TIME = 2;
}
