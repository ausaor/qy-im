package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.qy.implatform.entity.RegionGroupMember;

/**
 * 地区群聊常驻用户表
 *
 * @author Polaris
 * @since 2024-10-26
 */
public interface RegionGroupMemberMapper extends BaseMapper<RegionGroupMember> {
    @Select(
            "SELECT EXISTS ( " +
                    "   SELECT 1 " +
                    "   FROM im_region_group_member m1 " +
                    "   JOIN im_region_group_member m2 ON m1.region_group_id = m2.region_group_id " +
                    "   WHERE m1.user_id = #{userId1} " +
                    "     AND m2.user_id = #{userId2} " +
                    "     AND m1.quit = 0 " +
                    "     AND m2.quit = 0 " +
                    ") AS in_same_group"
    )
    Boolean existsInSameGroup(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2
    );
}
