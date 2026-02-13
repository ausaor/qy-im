package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.EmoAlbum;
import xyz.qy.implatform.mapper.EmoAlbumMapper;
import xyz.qy.implatform.service.IEmoAlbumService;

@Service
public class IEmoAlbumServiceImpl extends ServiceImpl<EmoAlbumMapper, EmoAlbum> implements IEmoAlbumService {
}
