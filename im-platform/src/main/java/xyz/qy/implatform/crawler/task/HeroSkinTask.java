//package xyz.qy.implatform.crawler.task;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import us.codecraft.webmagic.Spider;
//import xyz.qy.implatform.crawler.pipeline.HeroSkinPipeline;
//import xyz.qy.implatform.crawler.processor.HeroSkinProcessor;
//import xyz.qy.implatform.entity.HeroInfo;
//import xyz.qy.implatform.service.IHeroInfoService;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * 爬取王者荣耀英雄皮肤数据定时任务
// */
//@Component
//public class HeroSkinTask {
//
//    @Resource
//    private HeroSkinPipeline heroSkinPipeline;
//
//    @Resource
//    private HeroSkinProcessor heroSkinProcessor;
//
//    @Resource
//    private IHeroInfoService heroInfoService;
//
//    //@Scheduled(cron = "0 0/3 * * * ?")
//    public void startTask() {
//        LambdaQueryWrapper<HeroInfo> wrapper = new LambdaQueryWrapper<>();
//        Page<HeroInfo> page = new Page<>();
//        wrapper.eq(HeroInfo::getIsCrawl, false);
//
//        page.setCurrent(1L);
//        page.setSize(10L);
//
//        List<HeroInfo> heroInfoList = heroInfoService.page(page, wrapper).getRecords();
//        if (CollectionUtils.isEmpty(heroInfoList)) {
//            return;
//        }
//
//        String[] urls = heroInfoList.stream().map(HeroInfo::getHeroDetailUrl).toArray(String[]::new);
//        Spider spider = Spider.create(heroSkinProcessor);
//        spider.addUrl(urls);
//        spider.addPipeline(heroSkinPipeline);
//        spider.thread(3);
//        spider.start();
//
//        heroInfoList.forEach(item -> item.setIsCrawl(true));
//        heroInfoService.updateBatchById(heroInfoList);
//    }
//}
