package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.EmoImg;
import xyz.qy.implatform.mapper.EmoImgMapper;
import xyz.qy.implatform.service.IEmoImgService;

@Service
public class IEmoImgServiceImpl extends ServiceImpl<EmoImgMapper, EmoImg> implements IEmoImgService {
}