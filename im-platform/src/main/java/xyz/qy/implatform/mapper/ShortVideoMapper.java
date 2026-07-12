package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import xyz.qy.implatform.dto.ShortVideoQueryDTO;
import xyz.qy.implatform.entity.ShortVideo;

public interface ShortVideoMapper extends BaseMapper<ShortVideo> {
    /**
     * 分页查询推荐视频
     *
     * @param page 分页对象
     * @param dto 查询DTO
     * @return 视频列表
     */
    Page<ShortVideo> getRecommendShortVideos(@Param("page") Page<Object> page, @Param("dto") ShortVideoQueryDTO dto);

    /**
     * 分页查询所有角色视频
     *
     * @param page 分页对象
     * @param dto 查询DTO
     * @return 视频列表
     */
    Page<ShortVideo> getAllCharacterShortVideos(@Param("page") Page<Object> page, @Param("dto") ShortVideoQueryDTO dto);

    /**
     * 分页查询群角色短视频
     *
     * @param page 分页对象
     * @param dto 查询DTO
     * @return 视频列表
     */
    Page<ShortVideo> getGroupCharacterShortVideos(@Param("page") Page<Object> page, @Param("dto") ShortVideoQueryDTO dto);
}
