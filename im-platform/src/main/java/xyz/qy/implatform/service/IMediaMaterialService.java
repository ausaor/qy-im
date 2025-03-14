package xyz.qy.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.MediaMaterial;
import xyz.qy.implatform.vo.MediaMaterialVO;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-05-02 09:15
 **/
public interface IMediaMaterialService extends IService<MediaMaterial> {

    /**
     * 获取登录页展示素材
     *
     * @param vo 入参VO
     * @return 展示素材
     */
    MediaMaterialVO getLoginPagePlayMediaMaterial(MediaMaterialVO vo);

    /**
     * 获取首页展示素材
     *
     * @param vo 入参VO
     * @return 展示素材
     */
    MediaMaterialVO getHomePlayMediaMaterial(MediaMaterialVO vo);

    /**
     * 设置登录页展示素材
     *
     * @param sort 排序号
     * @return 展示素材
     */
    MediaMaterialVO setLoginPagePlayMediaMaterial(Integer sort);

    /**
     * 给媒体数据生成随机排序号
     */
    void generateRandomSort();

    /**
     * 获取最大排序号
     *
     * @return 最大排序号
     */
    int getMaxSort();
}
