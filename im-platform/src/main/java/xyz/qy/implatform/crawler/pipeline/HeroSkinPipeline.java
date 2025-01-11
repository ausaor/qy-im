package xyz.qy.implatform.crawler.pipeline;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import xyz.qy.implatform.entity.HeroSkin;
import xyz.qy.implatform.mapper.HeroSkinMapper;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class HeroSkinPipeline implements Pipeline {
    @Resource
    private HeroSkinMapper heroSkinMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        List<HeroSkin> heroSkinList = resultItems.get("heroSkinList");
        if (CollectionUtils.isEmpty(heroSkinList)) {
            return;
        }

        for (HeroSkin heroSkin : heroSkinList) {
            try {
                List<HeroSkin> skinList = heroSkinMapper.selectList(new LambdaQueryWrapper<HeroSkin>()
                        .eq(HeroSkin::getHeroId, heroSkin.getHeroId())
                        .eq(HeroSkin::getSkinName, heroSkin.getSkinName()));

                if (CollectionUtils.isEmpty(skinList)) {
                    int insert = heroSkinMapper.insert(heroSkin);
                    if (insert > 0) {
                        log.info(heroSkin.getHeroName() + "::" + heroSkin.getSkinName() + " insert success");
                    } else {
                        log.error(heroSkin.getHeroName() + "::" + heroSkin.getSkinName() + " insert success");
                    }
                }
            } catch (Exception e) {
                log.error("error", e);
            }

        }
    }
}
