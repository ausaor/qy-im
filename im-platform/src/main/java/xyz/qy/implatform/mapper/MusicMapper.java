package xyz.qy.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Update;
import xyz.qy.implatform.entity.Music;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-07-16 15:42
 **/
public interface MusicMapper extends BaseMapper<Music> {

    /**
     * 根据音乐ID增加播放次数
     * @param id 音乐ID
     */
    @Update("UPDATE im_music SET play_count = play_count + 1 WHERE id = #{id}")
    void increasePlayCount(Long id);
}