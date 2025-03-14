package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.Picture;
import xyz.qy.implatform.mapper.PictureMapper;
import xyz.qy.implatform.service.IPictureService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-07-08 10:38
 **/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements IPictureService {
    @Override
    public String getRandomPictureUrl() {
        Integer count = baseMapper.selectCount(new LambdaQueryWrapper<Picture>()
                .eq(Picture::getStatus, Constant.YES));
        Random random = new Random();
        // 生成随机id
        Integer id = random.nextInt(count) + 1;

        Picture picture = baseMapper.selectById(id);
        if (ObjectUtil.isNull(picture)) {
            return StringUtils.EMPTY;
        }
        return picture.getUrl();
    }
}
