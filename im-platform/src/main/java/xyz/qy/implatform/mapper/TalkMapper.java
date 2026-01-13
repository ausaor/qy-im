package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import xyz.qy.implatform.dto.TalkQueryDTO;
import xyz.qy.implatform.entity.Talk;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-11-19 21:37
 **/
public interface TalkMapper extends BaseMapper<Talk> {
    /**
     * 分页查询用户自己所有动态
     *
     * @param page     分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryMyTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询自己加好友的动态
     *
     * @param page     分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryMyAndFriendsTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询好友的动态
     *
     * @param page     分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryFriendsTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询群空间动态
     *
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryGroupTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询地区空间动态
     *
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryRegionTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询角色空间动态
     *
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryCharacterTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询群聊模版动态
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryGroupTemplateTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询群聊模版与角色动态
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryGroupTemplateCharactersTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页多个角色动态
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryCharactersTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);

    /**
     * 分页查询所有角色动态
     *
     * @param page 分页对象
     * @return 动态列表
     */
    Page<Talk> pageQueryAllCharactersTalkList(@Param("page") Page<Object> page);

    /**
     * 分页查询自己角色动态
     *
     * @param page 分页对象
     * @param queryDTO 查询DTO
     * @return 动态列表
     */
    Page<Talk> pageQueryMyCharactersTalkList(@Param("page") Page<Object> page, @Param("queryDTO") TalkQueryDTO queryDTO);
}
