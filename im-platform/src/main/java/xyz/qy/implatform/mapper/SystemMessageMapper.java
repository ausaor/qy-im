package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import xyz.qy.implatform.entity.SystemMessage;
import xyz.qy.implatform.vo.SystemMessageVO;

import java.util.Date;
import java.util.List;

/**
 * 系统消息
 *
 * @author Polaris
 * @since 2024-12-29
 */
public interface SystemMessageMapper extends BaseMapper<SystemMessage> {
    /**
     * 拉取离线系统消息
     *
     * @param startTime 开始时间
     * @param userId 用户id
     * @return 系统消息
     */
    List<SystemMessageVO> pullOfflineSystemMessage(@Param("startTime") Date startTime,
                                                   @Param("userId") Long userId,
                                                   @Param("minSeqNo") Long minSeqNo);

    Integer selectLastReadedSeqNo(@Param("pusherId") Long pusherId, @Param("userId") Long userId);
}
