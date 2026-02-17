package xyz.qy.implatform.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import xyz.qy.implatform.dto.EmoFavoriteDTO;
import xyz.qy.implatform.entity.EmoFavorite;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.mapper.EmoFavoriteMapper;
import xyz.qy.implatform.service.IEmoFavoriteService;
import xyz.qy.implatform.session.SessionContext;
import xyz.qy.implatform.session.UserSession;

@Service
public class IEmoFavoriteServiceImpl extends ServiceImpl<EmoFavoriteMapper, EmoFavorite> implements IEmoFavoriteService {

    @Override
    public void addEmoFavorite(EmoFavoriteDTO dto) {
        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();
        
        // 判断表情是否已收藏
        if (ObjectUtil.isNotNull(dto.getEmoId())) {
            Integer count = this.lambdaQuery()
                    .eq(EmoFavorite::getUserId, userId)
                    .eq(EmoFavorite::getEmoId, dto.getEmoId())
                    .count();
            if (count > 0) {
                throw new GlobalException("当前表情已收藏");
            }
        }
        // 判断用户收藏数量
        Integer count = this.lambdaQuery()
                .eq(EmoFavorite::getUserId, userId)
                .count();
        if (count >= 100) {
            throw new GlobalException("最多收藏100个表情");
        }
        EmoFavorite emoFavorite = new EmoFavorite();
        emoFavorite.setUserId(userId);
        emoFavorite.setEmoId(dto.getEmoId());
        emoFavorite.setAlbumId(dto.getAlbumId());
        emoFavorite.setImgUrl(dto.getImgUrl());
        this.save(emoFavorite);
    }

    @Override
    public void deleteEmoFavorite(Long id) {
        if (id == null) {
            throw new GlobalException("参数错误");
        }

        UserSession session = SessionContext.getSession();
        Long userId = session.getUserId();

        LambdaQueryWrapper<EmoFavorite> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(EmoFavorite::getId, id);
        updateWrapper.eq(EmoFavorite::getUserId, userId);
        boolean remove = this.remove(updateWrapper);
        if (!remove) {
            throw new GlobalException("删除失败");
        }
    }
}