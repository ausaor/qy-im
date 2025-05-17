//package xyz.qy.implatform.crawler.pipeline;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//import us.codecraft.webmagic.ResultItems;
//import us.codecraft.webmagic.Task;
//import us.codecraft.webmagic.pipeline.Pipeline;
//import xyz.qy.implatform.entity.HeroInfo;
//import xyz.qy.implatform.mapper.HeroInfoMapper;
//
//import javax.annotation.Resource;
//import java.util.List;
//
//@Slf4j
//@Component
//public class HeroInfoPipeline implements Pipeline {
//    @Resource
//    private HeroInfoMapper heroInfoMapper;
//
//    @Override
//    public void process(ResultItems resultItems, Task task) {
//        List<HeroInfo> heroInfoList = resultItems.get("heroInfoList");
//        if (CollectionUtils.isNotEmpty(heroInfoList)){
//            for (HeroInfo heroInfo : heroInfoList){
//                if (StringUtils.isNotBlank(heroInfo.getHeroId())){
//                    LambdaQueryWrapper<HeroInfo> wrapper = new LambdaQueryWrapper<>();
//                    wrapper.eq(HeroInfo::getHeroId,heroInfo.getHeroId());
//                    List<HeroInfo> list = heroInfoMapper.selectList(wrapper);
//                    if (CollectionUtils.isEmpty(list)){
//                        heroInfoMapper.insert(heroInfo);
//                    }
//                }
//            }
//        }
//    }
//}
