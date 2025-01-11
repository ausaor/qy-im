package xyz.qy.implatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;
import xyz.qy.implatform.crawler.pipeline.HeroInfoPipeline;
import xyz.qy.implatform.crawler.processor.HeroInfoProcessor;
import xyz.qy.implatform.crawler.task.DownloadHeroAvatarTimerTask;
import xyz.qy.implatform.crawler.task.DownloadHeroSkinTimerTask;
import xyz.qy.implatform.entity.HeroInfo;
import xyz.qy.implatform.entity.HeroSkin;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.service.IHeroInfoService;
import xyz.qy.implatform.service.IHeroSkinService;
import xyz.qy.implatform.service.common.CrawlerService;
import xyz.qy.implatform.service.common.ReadImageService;
import xyz.qy.implatform.util.AsyncManager;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/openApi")
@RestController
public class CrawlerController {
    @Resource
    private IHeroInfoService heroInfoService;

    @Resource
    private IHeroSkinService heroSkinService;

    @Resource
    private HeroInfoPipeline heroInfoPipeline;

    @Resource
    private HeroInfoProcessor heroInfoProcessor;

    @Resource
    private ReadImageService readImageService;

    @Resource
    private CrawlerService crawlerService;

    @ApiOperation(value = "爬取王者荣耀英雄数据", notes = "爬取王者荣耀英雄数据")
    @GetMapping(value = "/crawlerWzryHero")
    public Result crawlerHeroInfoTask() {
        Spider spider = Spider.create(heroInfoProcessor);
        spider.addUrl("https://pvp.qq.com/web201605/herolist.shtml");
        spider.addPipeline(heroInfoPipeline);
        spider.thread(1);
        spider.start();
        return ResultUtils.success();
    }

    @ApiOperation(value = "下载英雄头像", notes = "下载英雄头像")
    @GetMapping(value = "/downloadHeroAvatarByCate")
    public Result downloadHeroAvatar(@RequestParam("category") String category, @RequestParam("path") String path) {
        LambdaQueryWrapper<HeroInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeroInfo::getCategory, category);
        wrapper.eq(HeroInfo::getIsCrawl, false);
        wrapper.last("limit 50");

        List<HeroInfo> heroInfoList = heroInfoService.list(wrapper);
        for (HeroInfo heroInfo : heroInfoList) {
            DownloadHeroAvatarTimerTask task = new DownloadHeroAvatarTimerTask();
            task.setHeroInfo(heroInfo);
            task.setPreFixPath(path);
            AsyncManager.me().execute(task);
        }

        return ResultUtils.success();
    }

    @ApiOperation(value = "下载英雄皮肤头像", notes = "下载英雄皮肤头像")
    @GetMapping(value = "/downloadHeroSkinAvatarByCate")
    public Result downloadHeroSkin(@RequestParam("category") String category, @RequestParam("skinAvatarPath") String skinAvatarPath) {
        LambdaQueryWrapper<HeroSkin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HeroSkin::getCategory, category);
        wrapper.eq(HeroSkin::getIsCrawl, 0);
        wrapper.last("limit 6");

        List<HeroSkin> heroSkinList = heroSkinService.list(wrapper);
        if (CollectionUtils.isEmpty(heroSkinList)) {
            return ResultUtils.success();
        }
        for (HeroSkin heroSkin : heroSkinList) {
            DownloadHeroSkinTimerTask task = new DownloadHeroSkinTimerTask();
            task.setHeroSkin(heroSkin);
            task.setSkinAvatarPath(skinAvatarPath);
            AsyncManager.me().execute(task);
        }

        return ResultUtils.success();
    }

    @ApiOperation(value = "从磁盘读取文件")
    @GetMapping("/readImageFromDisk")
    public Result readImageFromDisk(String avatarPath, String skinAvatarPath, String urlPre, Long templateGroupId) {
        readImageService.readTemplateCharacterImageFromDisk(avatarPath, skinAvatarPath, urlPre, templateGroupId);
        return ResultUtils.success();
    }

    @GetMapping("/getWzryHeroInfo")
    public Result getWzryHeroInfo() {
        crawlerService.getWzryHeroInfo();
        return ResultUtils.success();
    }

    @GetMapping("/parseWzryHeroSkinHtml")
    public Result parseWzryHeroSkinHtml() {
        crawlerService.parseWzryHeroSkinHtml();
        return ResultUtils.success();
    }

    @GetMapping("/parseWzryHeroVoiceJson")
    public Result parseWzryHeroVoiceJson() {
        crawlerService.parseWzryHeroVoiceJson();
        return ResultUtils.success();
    }

    @GetMapping("/parseWzryHeroVoiceHtml")
    public Result parseWzryHeroVoiceHtml(@RequestParam("heroName") String heroName) {
        crawlerService.parseWzryHeroVoiceHtml(heroName);
        return ResultUtils.success();
    }

    @GetMapping("/readLOLHerosJson")
    public Result readLOLHerosJson() {
        crawlerService.readLOLHerosJson();
        return ResultUtils.success();
    }

    @GetMapping("/readLOLMHerosJson")
    public Result readLOLMHerosJson() {
        crawlerService.readLOLMHerosJson();
        return ResultUtils.success();
    }

    @GetMapping("/parseDreamThreeKingHerosHtml")
    public Result parseDreamThreeKingHerosHtml() {
        crawlerService.parseDreamThreeKingHerosHtml();
        return ResultUtils.success();
    }

    @GetMapping("/parseSpaceTimeCallHerosHtml")
    public Result parseSpaceTimeCallHerosHtml() {
        crawlerService.parseSpaceTimeCallHerosHtml();
        return ResultUtils.success();
    }
}
