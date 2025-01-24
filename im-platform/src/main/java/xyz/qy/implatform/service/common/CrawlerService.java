package xyz.qy.implatform.service.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.qy.implatform.entity.HeroInfo;
import xyz.qy.implatform.entity.HeroSkin;
import xyz.qy.implatform.entity.HeroWord;
import xyz.qy.implatform.service.IHeroInfoService;
import xyz.qy.implatform.service.IHeroSkinService;
import xyz.qy.implatform.service.IHeroWordService;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CrawlerService {

    private static final String HERO_PROFILE_URL = "https://game.gtimg.cn/images/yxzj/img201606/heroimg/";
    private static final String HERO_DETAIL_URL = "https://pvp.qq.com/web201605/herodetail/";

    @Resource
    private IHeroInfoService heroInfoService;

    @Resource
    private IHeroSkinService heroSkinService;

    @Resource
    private IHeroWordService heroWordService;

    public void getWzryHeroInfo() {
        String string = FileUtil.readUtf8String("G:/idea_workspace/data/wzry/herolist-20241230.json");

        List<HeroInfo> heroInfoList = new ArrayList<>();
        JSONArray jsonArray = JSONUtil.parseArray(string);
        for (int i = 0; i < jsonArray.size(); i++) {
            Object object = jsonArray.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(object);
            log.info(jsonObject.getStr("cname"));
            HeroInfo heroInfo = new HeroInfo();
            heroInfo.setCategory("王者荣耀");
            heroInfo.setHeroId(jsonObject.getStr("ename"));
            heroInfo.setHeroName(jsonObject.getStr("cname"));
            heroInfo.setHeroProfileUrl(HERO_PROFILE_URL + heroInfo.getHeroId() + "/" + heroInfo.getHeroId() + ".jpg");
            heroInfo.setHeroDetailUrl(HERO_DETAIL_URL + jsonObject.getStr("id_name") + ".shtml");
            heroInfoList.add(heroInfo);
        }

        heroInfoService.saveBatch(heroInfoList);
    }

    public void parseWzryHeroSkinHtml() {
        try {
            Document document = Jsoup.parse(new File("E:/image/wzry/heroSkin.html"), "utf8");
            Elements lis = document.getElementsByTag("li");
            HeroInfo heroInfo = null;
            List<HeroSkin> heroSkinList = new ArrayList<>();
            for (Element li : lis) {
                HeroSkin heroSkin = new HeroSkin();
                heroSkin.setCategory("王者荣耀");
                Element img = li.getElementsByTag("img").first();
                String src = img.attr("src");
                Elements p = li.getElementsByTag("p");
                String skinName = p.text();

                String[] arr = src.split("/");
                String heroId = null;
                if (arr.length > 2) {
                    heroId = arr[arr.length - 2];
                }

                if (ObjectUtil.isNull(heroInfo)) {
                    heroInfo = heroInfoService.lambdaQuery().eq(HeroInfo::getHeroId, heroId)
                            .eq(HeroInfo::getCategory, "王者荣耀").one();
                    if (ObjectUtil.isNull(heroInfo)) {
                        return;
                    }
                }
                heroSkin.setHeroId(heroInfo.getHeroId());
                heroSkin.setHeroName(heroInfo.getHeroName());
                heroSkin.setSkinProfileUrl("https:" + src);
                heroSkin.setSkinName(skinName);
                log.info("src:{}", src);
                log.info("skin-name:{}", skinName);

                heroSkinList.add(heroSkin);
            }

            if (CollectionUtil.isNotEmpty(heroSkinList)) {
                heroSkinList.forEach(item -> {
                    LambdaUpdateWrapper<HeroSkin> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.set(HeroSkin::getSkinProfileUrl, item.getSkinProfileUrl());
                    updateWrapper.eq(HeroSkin::getHeroId, item.getHeroId());
                    updateWrapper.eq(HeroSkin::getCategory, item.getCategory());
                    updateWrapper.eq(HeroSkin::getSkinName, item.getSkinName());

                    heroSkinService.saveOrUpdate(item, updateWrapper);
                });

                //heroSkinService.saveOrUpdateBatch(heroSkinList);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    public void parseWzryHeroVoiceJson() {
        String string = FileUtil.readUtf8String("G:/idea_workspace/data/wzry/hero-voice-list.json");
        JSONArray jsonArray = JSONUtil.parseArray(string);

        for (int i = 0; i < jsonArray.size(); i++) {
            Object object = jsonArray.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(object);
            String heroId = jsonObject.getStr("yxid_a7");
            HeroInfo heroInfo = heroInfoService.lambdaQuery()
                    .eq(HeroInfo::getCategory, "王者荣耀").eq(HeroInfo::getHeroId, heroId).one();
            if (ObjectUtil.isNull(heroInfo)) {
                continue;
            }
            JSONArray voiceArr = jsonObject.getJSONArray("yy_4e");
            if (CollectionUtil.isNotEmpty(voiceArr)) {
                List<HeroWord> heroWords = new ArrayList<>();
                for (int j = 0; j < voiceArr.size(); j++) {
                    HeroWord heroWord = new HeroWord();
                    heroWord.setCategory("王者荣耀");
                    heroWord.setHeroId(heroId);
                    heroWord.setHeroName(heroInfo.getHeroName());
                    Object obj = voiceArr.get(j);
                    JSONObject voiceObj = JSONUtil.parseObj(obj);
                    String word = voiceObj.getStr("yywa1_f2");
                    String voiceUrl = "https:" + voiceObj.getStr("yyyp_9a");
                    log.info("heroName:{},word:{},voiceUrl:{}", heroInfo.getHeroName(), word, voiceUrl);
                    heroWord.setWord(word);
                    heroWord.setVoiceUrl(voiceUrl);
                    heroWords.add(heroWord);
                }
                heroWordService.saveBatch(heroWords);
            }
        }
    }

    public void parseWzryHeroVoiceHtml(String heroName) {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/wzry/heros-voice.html"), "utf8");
            Elements aEles = document.getElementsByTag("a");
            HeroInfo heroInfo = heroInfoService.lambdaQuery().eq(HeroInfo::getCategory, "王者荣耀")
                    .eq(HeroInfo::getHeroName, heroName).one();
            if (ObjectUtil.isNull(heroInfo)) {
                return;
            }
            for (Element aEle : aEles) {
                String voiceUrl = "https:" + aEle.attr("data-mp3");
                Elements span = aEle.getElementsByTag("span");
                String word = span.text();
                List<HeroWord> wordList = heroWordService.lambdaQuery()
                        .eq(HeroWord::getCategory, "王者荣耀")
                        .eq(HeroWord::getHeroName, heroInfo.getHeroName())
                        .eq(HeroWord::getWord, word).list();
                log.info("heroName:{},word:{},voiceUrl:{}", heroName, word, voiceUrl);
                if (CollectionUtil.isNotEmpty(wordList)) {
                    continue;
                }
                HeroWord heroWord = new HeroWord();
                heroWord.setCategory("王者荣耀");
                heroWord.setHeroId(heroInfo.getHeroId());
                heroWord.setHeroName(heroInfo.getHeroName());
                heroWord.setWord(word);
                heroWord.setVoiceUrl(voiceUrl);
                heroWordService.save(heroWord);
            }
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    public void readLOLHerosJson() {
        String string = FileUtil.readUtf8String("G:/idea_workspace/data/LOL/herolist.json");

        List<HeroInfo> heroInfoList = new ArrayList<>();
        JSONArray jsonArray = JSONUtil.parseArray(string);
        for (int i = 0; i < jsonArray.size(); i++) {
            Object object = jsonArray.get(i);
            JSONObject jsonObject = JSONUtil.parseObj(object);
            HeroInfo heroInfo = new HeroInfo();
            heroInfo.setCategory("LOL");
            heroInfo.setHeroId(jsonObject.getStr("heroId"));
            heroInfo.setHeroName(jsonObject.getStr("title"));
            heroInfo.setHeroProfileUrl("https://game.gtimg.cn/images/lol/act/img/champion/" + jsonObject.getStr("alias") + ".png");
            heroInfoList.add(heroInfo);
        }

        heroInfoService.saveBatch(heroInfoList);
    }

    public void readLOLMHerosJson() {
        String string = FileUtil.readUtf8String("G:/idea_workspace/data/Lolm/herolist.json");

        List<HeroInfo> heroInfoList = new ArrayList<>();
        JSONObject json = JSONUtil.parseObj(string);

        json.forEach((key, value) -> {
            JSONObject jsonObject = JSONUtil.parseObj(value);
            HeroInfo heroInfo = new HeroInfo();
            heroInfo.setCategory("LOLM");
            heroInfo.setHeroId(jsonObject.getStr("heroId"));
            heroInfo.setHeroName(jsonObject.getStr("name"));
            heroInfo.setHeroProfileUrl(jsonObject.getStr("avatar"));
            heroInfoList.add(heroInfo);
        });
        heroInfoService.saveBatch(heroInfoList);
    }

    /**
     * 解析梦三国英雄数据html
     */
    public void parseDreamThreeKingHerosHtml() {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/梦三国/heros.html"), "utf8");
            Elements lis = document.getElementsByTag("li");
            List<HeroInfo> heroInfoList = new ArrayList<>();
            int heroId = 1;
            for (Element li : lis) {
                Elements pEles = li.getElementsByTag("p");
                for (Element p : pEles) {
                    HeroInfo heroInfo = new HeroInfo();
                    heroInfo.setCategory("梦三国");
                    heroInfo.setHeroId(String.valueOf(heroId));
                    Element img = p.getElementsByTag("img").first();
                    String src = "https:" + img.attr("src");
                    Elements span = p.getElementsByTag("span");
                    String heroName = span.text();
                    log.info("src:{}", src);
                    log.info("heroName:{}", heroName);
                    heroInfo.setHeroName(heroName.replaceAll("梦\\*", "梦-"));
                    heroInfo.setHeroProfileUrl(src);
                    heroId++;
                    heroInfoList.add(heroInfo);
//                    LambdaUpdateWrapper<HeroInfo> updateWrapper = new LambdaUpdateWrapper<>();
//                    updateWrapper.eq(HeroInfo::getCategory, heroInfo.getCategory());
//                    updateWrapper.eq(HeroInfo::getHeroId, heroInfo.getHeroId());
//                    updateWrapper.set(HeroInfo::getHeroName, heroInfo.getHeroName());
//
//                    heroInfoService.update(updateWrapper);
                }
            }
            heroInfoService.saveBatch(heroInfoList, 50);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 解析时空召唤英雄数据html
     */
    public void parseSpaceTimeCallHerosHtml() {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/时空召唤/heros.html"), "utf8");
            Elements aEles = document.getElementsByTag("a");
            List<HeroInfo> heroInfoList = new ArrayList<>();
            int heroId = 1;
            for (Element aEle : aEles) {
                Element img = aEle.getElementsByTag("img").first();
                String src = "https://moba.yh.cn/moba/v2/pc/" + img.attr("src").substring(2);
                Element span = aEle.getElementsByTag("span").first();
                String heroName = span.text();
                log.info("src:{}", src);
                log.info("heroName:{}", heroName);
                HeroInfo heroInfo = new HeroInfo();
                heroInfo.setCategory("时空召唤");
                heroInfo.setHeroId(String.valueOf(heroId));
                heroInfo.setHeroName(heroName);
                heroInfo.setHeroProfileUrl(src);
                heroId++;
                heroInfoList.add(heroInfo);
            }
            heroInfoService.saveBatch(heroInfoList, 50);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 解析原神角色数据html
     */
    public void parseYuanShenCharactersHtml() {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/YuanShen/heros.html"), "utf8");
            Elements lis = document.getElementsByTag("li");
            List<HeroInfo> heroInfoList = new ArrayList<>();
            int heroId = 1;
            for (Element liEle : lis) {
                Element img = liEle.getElementsByTag("img").first();
                String src = img.attr("src");
                Element p = liEle.getElementsByTag("p").first();
                String heroName = p.text();
                log.info("src:{}", src);
                log.info("heroName:{}", heroName);
                HeroInfo heroInfo = new HeroInfo();
                heroInfo.setCategory("原神");
                heroInfo.setHeroId(String.valueOf(heroId));
                heroInfo.setHeroName(heroName);
                heroInfo.setHeroProfileUrl(src);
                heroId++;
                heroInfoList.add(heroInfo);
            }
            heroInfoService.saveBatch(heroInfoList, 50);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 解析决战巅峰英雄数据html
     * https://m.ali213.net/jsdf/yxtj/
     */
    public void parseBattleAtThePeakHeroHtml() {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/BattleAtThePeak/heros.html"), "utf8");
            Elements aEles = document.getElementsByTag("a");
            List<HeroInfo> heroInfoList = new ArrayList<>();
            int heroId = 1;
            for (Element aEle : aEles) {
                Element img = aEle.getElementsByTag("img").first();
                String src = "https:" + img.attr("src");
                Element span = aEle.getElementsByTag("span").first();
                String heroName = span.text();
                log.info("src:{}", src);
                log.info("heroName:{}", heroName);
                HeroInfo heroInfo = new HeroInfo();
                heroInfo.setCategory("决战巅峰");
                heroInfo.setHeroId(String.valueOf(heroId));
                heroInfo.setHeroName(heroName);
                heroInfo.setHeroProfileUrl(src);
                heroId++;
                heroInfoList.add(heroInfo);
            }
            heroInfoService.saveBatch(heroInfoList, 50);
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * 解析非人学院英雄数据html
     */
    @Transactional
    public void parseInhumanCollegeHeroHtml() {
        try {
            Document document = Jsoup.parse(new File("G:/idea_workspace/data/InhumanCollege/heros.html"), "utf8");
            Elements uls = document.getElementsByTag("ul");
            List<HeroInfo> heroInfoList = new ArrayList<>();
            int heroId = 1;
            for (Element ul : uls) {
                Elements lis = ul.getElementsByTag("li");
                for (Element li : lis) {
                    Element img = li.getElementsByTag("img").first();
                    String src = img.attr("src");
                    Elements p = li.getElementsByTag("p");
                    String heroName = p.text();
                    log.info("src:{}", src);
                    log.info("heroName:{}", heroName);
                    HeroInfo heroInfo = new HeroInfo();
                    heroInfo.setHeroId(String.valueOf(heroId));
                    heroInfo.setCategory("非人学院");
                    heroInfo.setHeroName(heroName);
                    heroInfo.setHeroProfileUrl(src);
                    heroId++;
                    heroInfoList.add(heroInfo);
                }
            }
            heroInfoService.saveBatch(heroInfoList, 50);
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}
