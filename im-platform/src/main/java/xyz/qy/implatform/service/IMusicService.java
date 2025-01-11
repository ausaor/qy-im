package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Music;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-07-16 15:43
 **/
public interface IMusicService extends IService<Music> {

    void crawlMusic(Integer id);
}
