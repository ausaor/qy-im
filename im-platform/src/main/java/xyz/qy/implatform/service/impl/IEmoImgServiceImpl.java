package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.EmoAlbum;
import xyz.qy.implatform.entity.EmoImg;
import xyz.qy.implatform.mapper.EmoAlbumMapper;
import xyz.qy.implatform.mapper.EmoImgMapper;
import xyz.qy.implatform.service.IEmoAlbumService;
import xyz.qy.implatform.service.IEmoImgService;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.EmoAlbumVO;
import xyz.qy.implatform.vo.EmoImgVO;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IEmoImgServiceImpl extends ServiceImpl<EmoImgMapper, EmoImg> implements IEmoImgService {
    @Resource
    private EmoAlbumMapper emoAlbumMapper;

    @Override
    public List<EmoAlbumVO> getEmoAlbumImgList() {
        LambdaQueryWrapper<EmoAlbum> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmoAlbum::getDeleted, false);
        wrapper.select(EmoAlbum::getId, EmoAlbum::getName, EmoAlbum::getLogoUrl);
        List<EmoAlbum> emoAlbumList = emoAlbumMapper.selectList(wrapper);

        if (CollectionUtils.isEmpty(emoAlbumList)) {
            return Collections.emptyList();
        }

        List<Integer> albumIds = emoAlbumList.stream().map(EmoAlbum::getId).collect(Collectors.toList());
        List<EmoImg> emoImgList = this.lambdaQuery().eq(EmoImg::getDeleted, false)
                .in(EmoImg::getAlbumId, albumIds).list();

        Map<Integer, List<EmoImg>> emoImgMap = emoImgList.stream().collect(Collectors.groupingBy(EmoImg::getAlbumId));

        List<EmoAlbumVO> emoAlbumVos = BeanUtils.copyPropertiesList(emoAlbumList, EmoAlbumVO.class);

        emoAlbumVos.forEach(emoAlbumVO -> {
            List<EmoImg> imgList = emoImgMap.getOrDefault(emoAlbumVO.getId(), Collections.emptyList());
            emoAlbumVO.setEmoImgList(BeanUtils.copyPropertiesList(imgList, EmoImgVO.class));
        });

        return emoAlbumVos;
    }
}