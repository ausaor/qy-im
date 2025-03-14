package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.Picture;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-07-08 10:37
 **/
public interface IPictureService extends IService<Picture> {

    /**
     * 获取一张随机图片链接
     *
     * @return
     */
    String getRandomPictureUrl();
}
