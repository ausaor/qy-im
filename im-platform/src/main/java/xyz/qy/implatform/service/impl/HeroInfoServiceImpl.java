package xyz.qy.implatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.HeroInfo;
import xyz.qy.implatform.mapper.HeroInfoMapper;
import xyz.qy.implatform.service.IHeroInfoService;

@Service
public class HeroInfoServiceImpl extends ServiceImpl<HeroInfoMapper, HeroInfo> implements IHeroInfoService {
}
