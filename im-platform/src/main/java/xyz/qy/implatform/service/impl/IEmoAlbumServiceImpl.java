package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.dto.EmoAlbumDTO;
import xyz.qy.implatform.entity.EmoAlbum;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.EmoAlbumMapper;
import xyz.qy.implatform.service.IEmoAlbumService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;
import xyz.qy.implatform.util.BeanUtils;
import xyz.qy.implatform.vo.EmoAlbumVO;

import java.util.List;

@Service
public class IEmoAlbumServiceImpl extends ServiceImpl<EmoAlbumMapper, EmoAlbum> implements IEmoAlbumService {
    @Override
    public void addEmoAlbum(EmoAlbumDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        EmoAlbum emoAlbum = new EmoAlbum();
        emoAlbum.setName(dto.getName());
        emoAlbum.setLogoUrl(dto.getLogoUrl());
        emoAlbum.setCreateBy(userId);

        this.save(emoAlbum);
    }

    @Override
    public void updateEmoAlbum(EmoAlbumDTO dto) {
        if (ObjectUtil.isNull(dto.getId())) {
            throw new GlobalException("参数异常");
        }
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        EmoAlbum emoAlbum = this.getById(dto.getId());
        if (ObjectUtil.isNull(emoAlbum)) {
            throw new GlobalException("相册不存在");
        }

        emoAlbum.setName(dto.getName());
        emoAlbum.setLogoUrl(dto.getLogoUrl());
        emoAlbum.setUpdateBy(userId);
        this.updateById(emoAlbum);
    }

    @Override
    public void deleteEmoAlbum(Long id) {
        EmoAlbum emoAlbum = this.getById(id);

        if (ObjectUtil.isNull(emoAlbum)) {
            throw new GlobalException("相册不存在");
        }
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        emoAlbum.setDeleted(true);
        emoAlbum.setUpdateBy(userId);
        this.updateById(emoAlbum);
    }

    @Override
    public List<EmoAlbumVO> getEmoAlbumList() {
        List<EmoAlbum> albumList = this.lambdaQuery()
                .eq(EmoAlbum::getDeleted, false)
                .list();
        return BeanUtils.copyProperties(albumList, EmoAlbumVO.class);
    }
}
