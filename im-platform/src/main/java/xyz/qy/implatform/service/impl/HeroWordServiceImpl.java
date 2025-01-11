package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.HeroWord;
import xyz.qy.implatform.mapper.HeroWordMapper;
import xyz.qy.implatform.service.IHeroWordService;

@Service
public class HeroWordServiceImpl extends ServiceImpl<HeroWordMapper, HeroWord> implements IHeroWordService {
}
