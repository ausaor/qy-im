package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.HeroSkin;
import xyz.qy.implatform.mapper.HeroSkinMapper;
import xyz.qy.implatform.service.IHeroSkinService;

@Service
public class HeroSkinServiceImpl extends ServiceImpl<HeroSkinMapper, HeroSkin> implements IHeroSkinService {
}
