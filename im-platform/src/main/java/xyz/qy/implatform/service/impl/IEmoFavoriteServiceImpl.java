package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.EmoFavorite;
import xyz.qy.implatform.mapper.EmoFavoriteMapper;
import xyz.qy.implatform.service.IEmoFavoriteService;

@Service
public class IEmoFavoriteServiceImpl extends ServiceImpl<EmoFavoriteMapper, EmoFavorite> implements IEmoFavoriteService {
}