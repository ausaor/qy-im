package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.entity.DictData;
import xyz.qy.implatform.entity.MediaMaterial;
import xyz.qy.implatform.mapper.DictDataMapper;
import xyz.qy.implatform.mapper.MediaMaterialMapper;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.IPictureService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.util.RedisCache;
import xyz.qy.implatform.vo.MediaMaterialVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-05-02 09:16
 **/
@Slf4j
@Service
public class MediaMaterialServiceImpl extends ServiceImpl<MediaMaterialMapper, MediaMaterial> implements IMediaMaterialService {
    private static Random rand = new Random();

    @Autowired
    private RedisCache redisCache;

    @Resource
    private DictDataMapper dictDataMapper;

    @Autowired
    private IMediaMaterialService mediaMaterialService;

    @Autowired
    private IPictureService pictureService;

    @Override
    public MediaMaterialVO getLoginPagePlayMediaMaterial(MediaMaterialVO vo) {
        // 判断是否开启媒体播放
        String mediaSwitch = (String) redisCache.getCacheObject(RedisKey.ON_OFF_DISPLAY_MEDIA);
        // 缓存为空查询数据库
        if (StringUtils.isBlank(mediaSwitch)) {
            LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DictData::getDictType, Constant.MEDIA_SWITCH);
            DictData dictData = dictDataMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotNull(dictData)) {
                mediaSwitch = dictData.getDictValue();
                redisCache.setCacheObject(RedisKey.ON_OFF_DISPLAY_MEDIA, mediaSwitch, 30, TimeUnit.MINUTES);
            }
        }
        if (StringUtils.isBlank(mediaSwitch) || !Constant.YES_STR.equals(mediaSwitch)) {
            MediaMaterialVO mediaMaterialVO = new MediaMaterialVO();
            mediaMaterialVO.setShowMedia(false);
            return mediaMaterialVO;
        }

        return getMediaMaterial(vo);
    }

    @Override
    public MediaMaterialVO getHomePlayMediaMaterial(MediaMaterialVO vo) {
        // 判断是否开启媒体播放
        String mediaSwitch = redisCache.getCacheObject(RedisKey.ON_OFF_HOME_DISPLAY_MEDIA);
        // 缓存为空查询数据库
        if (StringUtils.isBlank(mediaSwitch)) {
            LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DictData::getDictType, Constant.HOME_MEDIA_SWITCH);
            DictData dictData = dictDataMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotNull(dictData)) {
                mediaSwitch = dictData.getDictValue();
                redisCache.setCacheObject(RedisKey.ON_OFF_HOME_DISPLAY_MEDIA, mediaSwitch, 30, TimeUnit.MINUTES);
            }
        }
        if (StringUtils.isBlank(mediaSwitch) || !Constant.YES_STR.equals(mediaSwitch)) {
            MediaMaterialVO mediaMaterialVO = new MediaMaterialVO();
            mediaMaterialVO.setShowMedia(false);
            return mediaMaterialVO;
        }
        return getMediaMaterial(vo);
    }

    private MediaMaterialVO getMediaMaterial(MediaMaterialVO vo) {
        // 判断传入参数排序号不为空，查询比入参排序号大1的素材
        if (ObjectUtil.isNotNull(vo.getSort())) {
            LambdaQueryWrapper<MediaMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.gt(MediaMaterial::getSort, vo.getSort());
            wrapper.eq(MediaMaterial::getStatus, Constant.YES);
            wrapper.eq(StringUtils.isNotBlank(vo.getType()), MediaMaterial::getType, vo.getType());
            wrapper.last("limit 1");
            List<MediaMaterial> materialList = baseMapper.selectList(wrapper);
            if (CollectionUtils.isNotEmpty(materialList)) {
                MediaMaterial mediaMaterial = materialList.get(0);
                getRandomPictureUrl(mediaMaterial);
                updateMediaMaterial(mediaMaterial);
                MediaMaterialVO mediaMaterialVO = BeanUtils.copyProperties(mediaMaterial, MediaMaterialVO.class);
                assert mediaMaterialVO != null;
                mediaMaterialVO.setShowMedia(true);
                return mediaMaterialVO;
            } else {
                // 为空说明是最后一条，查询第一条返回
                LambdaQueryWrapper<MediaMaterial> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.orderByAsc(MediaMaterial::getSort);
                queryWrapper.eq(MediaMaterial::getStatus, Constant.YES);
                queryWrapper.eq(StringUtils.isNotBlank(vo.getType()), MediaMaterial::getType, vo.getType());
                queryWrapper.last("limit 1");
                List<MediaMaterial> mediaMaterials = baseMapper.selectList(queryWrapper);
                if (CollectionUtils.isNotEmpty(mediaMaterials)) {
                    MediaMaterial mediaMaterial = mediaMaterials.get(0);
                    getRandomPictureUrl(mediaMaterial);
                    updateMediaMaterial(mediaMaterial);
                    MediaMaterialVO mediaMaterialVO = BeanUtils.copyProperties(mediaMaterial, MediaMaterialVO.class);
                    assert mediaMaterialVO != null;
                    mediaMaterialVO.setShowMedia(true);
                    return mediaMaterialVO;
                }
            }
        }

        // 先查询缓存
        MediaMaterialVO cacheObj = (MediaMaterialVO) redisCache.getCacheObject(RedisKey.LOGIN_MEDIA);
        if (ObjectUtil.isNotNull(cacheObj)) {
            return cacheObj;
        }

        // 入参排序号为空，随机返回一条媒体素材返回
        LambdaQueryWrapper<MediaMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MediaMaterial::getStatus, Constant.YES);

        Integer count = baseMapper.selectCount(lambdaQueryWrapper);
        Random random = new Random();
        // 生成随机排序号
        int sort = random.nextInt(count) + 1;

        lambdaQueryWrapper.eq(MediaMaterial::getSort, sort);
        // 查询生成的随机排序号的数据
        MediaMaterial mediaMaterial = baseMapper.selectOne(lambdaQueryWrapper);
        MediaMaterialVO mediaMaterialVO = new MediaMaterialVO();
        if (ObjectUtil.isNull(mediaMaterial)) {
            return mediaMaterialVO;
        }
        getRandomPictureUrl(mediaMaterial);
        updateMediaMaterial(mediaMaterial);
        MediaMaterialVO materialVO = BeanUtils.copyProperties(mediaMaterial, MediaMaterialVO.class);
        assert materialVO != null;
        materialVO.setShowMedia(true);
        redisCache.setCacheObject(RedisKey.LOGIN_MEDIA, materialVO, materialVO.getDisplayDuration(), TimeUnit.SECONDS);
        return materialVO;
    }

    private void getRandomPictureUrl(MediaMaterial mediaMaterial) {
        if (!"audio".equals(mediaMaterial.getType())) {
            return;
        }
        String pictureUrl = pictureService.getRandomPictureUrl();
        if (StringUtils.isNotBlank(pictureUrl)) {
            mediaMaterial.setCoverImage(pictureUrl);
        }
    }

    private void updateMediaMaterial(MediaMaterial mediaMaterial) {
        mediaMaterial.setStartTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, mediaMaterial.getDisplayDuration());
        mediaMaterial.setEndTime(calendar.getTime());
        mediaMaterial.setUpdateTime(new Date());
        this.baseMapper.updateById(mediaMaterial);
    }

    @Override
    public MediaMaterialVO setLoginPagePlayMediaMaterial(Integer sort) {
        MediaMaterialVO mediaMaterialVO = null;
        if (ObjectUtil.isNull(sort)) {
            return null;
        }
        LambdaQueryWrapper<MediaMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaMaterial::getSort, sort);
        wrapper.eq(MediaMaterial::getStatus, Constant.YES);
        wrapper.last("limit 1");
        List<MediaMaterial> materialList = baseMapper.selectList(wrapper);
        if (CollectionUtils.isNotEmpty(materialList)) {
            MediaMaterial mediaMaterial = materialList.get(0);
            getRandomPictureUrl(mediaMaterial);
            updateMediaMaterial(mediaMaterial);
            mediaMaterialVO = BeanUtils.copyProperties(mediaMaterial, MediaMaterialVO.class);
            assert mediaMaterialVO != null;
            mediaMaterialVO.setShowMedia(true);
        }
        redisCache.setCacheObject(RedisKey.LOGIN_MEDIA, mediaMaterialVO, mediaMaterialVO.getDisplayDuration(), TimeUnit.SECONDS);
        return mediaMaterialVO;
    }

    @Override
    public void generateRandomSort() {
        LambdaQueryWrapper<MediaMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MediaMaterial::getId, MediaMaterial::getSort);
        wrapper.eq(MediaMaterial::getStatus, Constant.YES);
        List<MediaMaterial> materialList = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(materialList)) {
            return;
        }
        Integer[] sortNumArr = new Integer[materialList.size()];
        int length = sortNumArr.length;
        for (int i = 0; i < length; i++) {
            sortNumArr[i] = i + 1;
        }
        shuffle(sortNumArr);

        Date now = new Date();
        for (int i = 0; i < materialList.size(); i++) {
            materialList.get(i).setSort(sortNumArr[i]);
            materialList.get(i).setUpdateTime(now);
        }
        mediaMaterialService.updateBatchById(materialList);
    }

    private <T> void swap(T[] a, int i, int j) {
        T temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private <T> void shuffle(T[] arr) {
        int length = arr.length;
        for (int i = length; i > 0; i--) {
            int randIndex = rand.nextInt(i);
            swap(arr, randIndex, i - 1);
        }
    }

    @Override
    public int getMaxSort() {
        LambdaQueryWrapper<MediaMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(MediaMaterial::getSort);
        queryWrapper.eq(MediaMaterial::getStatus, Constant.YES);
        queryWrapper.last("limit 1");
        MediaMaterial mediaMaterial = baseMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotNull(mediaMaterial)) {
            return mediaMaterial.getSort();
        }
        return 0;
    }
}
