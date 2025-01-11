package xyz.qy.implatform.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import xyz.qy.implatform.contant.Constant;
import xyz.qy.implatform.entity.MediaMaterial;
import xyz.qy.implatform.entity.Music;
import xyz.qy.implatform.enums.FilePathEnum;
import xyz.qy.implatform.mapper.MusicMapper;
import xyz.qy.implatform.service.IMediaMaterialService;
import xyz.qy.implatform.service.IMusicService;
import xyz.qy.implatform.strategy.impl.QiNiuUploadStrategyImpl;
import xyz.qy.implatform.util.FileUtils;
import xyz.qy.implatform.vo.MockMultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-07-16 15:43
 **/
@Slf4j
@Service
public class MusicServiceImpl extends ServiceImpl<MusicMapper, Music> implements IMusicService {
    @Autowired
    private QiNiuUploadStrategyImpl qiNiuUpload;

    @Autowired
    private IMediaMaterialService mediaMaterialService;

    @Override
    public void crawlMusic(Integer id) {
        LambdaQueryWrapper<Music> queryWrapper = new LambdaQueryWrapper<>();
        if (id != null) {
            queryWrapper.eq(Music::getId, id);
        }
        queryWrapper.eq(Music::getHasCrawl, Constant.NO);
        queryWrapper.last("limit 1");
        Music music = baseMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(music)) {
            return;
        }
        byte[] bytes = HttpUtil.downloadBytes(music.getMusicUrl());
        try (InputStream inputStream = new ByteArrayInputStream(bytes)){
            MultipartFile file = new MockMultipartFile(ContentType.APPLICATION_OCTET_STREAM.toString(), inputStream);
            // 获取文件扩展名
            String extName = FileUtils.getExtName(music.getMusicUrl());
            // 重新生成文件名
            String fileName = music.getMusicName() + extName;
            String url = qiNiuUpload.uploadFile(FilePathEnum.AUDIO.getPath()+"box-im/", fileName, file);

            if (StringUtils.isBlank(url)) {
                return;
            }
            music.setUpdateTime(DateUtil.date());
            music.setHasCrawl(Constant.YES);
            baseMapper.updateById(music);
            MediaMaterial mediaMaterial = new MediaMaterial();
            mediaMaterial.setTitle(music.getMusicName());
            mediaMaterial.setUrl(url);
            mediaMaterial.setType("audio");
            mediaMaterial.setFormat(extName.substring(1));
            mediaMaterial.setDisplayDuration(180);
            mediaMaterial.setStatus(Constant.YES);
            mediaMaterial.setSort(mediaMaterialService.getMaxSort() + 1);
            mediaMaterial.setCreateTime(DateUtil.date());
            mediaMaterial.setUpdateTime(DateUtil.date());
            mediaMaterialService.save(mediaMaterial);
            log.info("url:{}", url);
        } catch (IOException e) {
            log.error("error:{}", e.getMessage());
        }
    }
}
