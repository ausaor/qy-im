package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import xyz.qy.implatform.entity.GroupMember;

public interface GroupMemberMapper extends BaseMapper<GroupMember> {
    @Select(
            "SELECT EXISTS ( " +
                    "   SELECT 1 " +
                    "   FROM im_group_member m1 " +
                    "   JOIN im_group_member m2 ON m1.group_id = m2.group_id " +
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
