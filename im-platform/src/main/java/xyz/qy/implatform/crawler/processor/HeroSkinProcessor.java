package xyz.qy.implatform.crawler.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import xyz.qy.implatform.entity.HeroSkin;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class HeroSkinProcessor implements PageProcessor {
    @Override
    public void process(Page page) {
        System.getProperties().setProperty("webdriver.chrome.driver", "G:\\idea_workspace_study\\crawler\\chromedriver_win32\\chromedriver.exe");
        //创建谷歌浏览器驱动
        WebDriver driver = new ChromeDriver();

        try {
            Selectable selectable = page.getUrl();

            String url = page.getUrl().get();

            //进入页面
            driver.get(selectable.toString());

            //实际场景中可能会发生页面节点没有加载出来但是程序已经跑完的情况
            //可以利用显示等待，等待节点加载出来程序继续运行
            //但是不能无限等待
            //这里设置等待时长为5秒
            //超过这个时间就会抛出异常
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));

            WebElement wrapper = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='wrapper']")));

            WebElement crumb = wrapper.findElement(By.cssSelector("div.zkcontent > div.zk-con-box > div.zk-con3 > div.crumb"));

            // 王者荣耀英雄id
            String lastPath = url.substring(url.lastIndexOf("/") + 1);
            String[] arr = lastPath.split("\\.");
            String heroId = null;
            if (arr.length == 2) {
                heroId = arr[0];
            }
            if (StringUtils.isBlank(heroId)) {
                return;
            }

            String heroName = crumb.findElement(By.tagName("label")).getText();

            // 所有皮肤li
            List<WebElement> skinLis = wrapper.findElements(By.cssSelector("ul.pic-pf-list > li"));
            //List<WebElement> skinLis = ul.findElements(By.xpath("//ul[@class='pic-pf-list pic-pf-list3'] > li"));

            if (CollectionUtils.isEmpty(skinLis)) {
                return;
            }

            log.info(heroName + "共有皮肤数量：{}", skinLis.size());
            List<HeroSkin> heroSkinList = new LinkedList<>();
            for (WebElement skinLi : skinLis) {
                HeroSkin heroSkin = new HeroSkin();
                WebElement imgEle = skinLi.findElement(By.cssSelector("i > img"));
                WebElement pEle = skinLi.findElement(By.tagName("p"));
                String skinName = pEle.getText();

                // 皮肤头像链接
                String skinProfileUrl = imgEle.getAttribute("src");

                // 皮肤链接
                String skinUrl = "https:" + imgEle.getAttribute("data-imgname");
                log.info("皮肤名称：{}", skinName);
                log.info("皮肤头像链接：{}", skinProfileUrl);
                log.info("皮肤链接：{}", skinUrl);

                heroSkin.setHeroId(heroId)
                        .setHeroName(heroName)
                        .setSkinName(skinName)
                        .setSkinUrl(skinUrl)
                        .setSkinProfileUrl(skinProfileUrl)
                        .setCategory("wzry");
                heroSkinList.add(heroSkin);
            }

            if (CollectionUtils.isNotEmpty(heroSkinList)) {
                page.putField("heroSkinList", heroSkinList);
            }
        } catch (Exception e) {
            log.error("error", e);
        } finally {
            driver.close();
        }
    }

    /**
     * 对爬虫进行配置
     * 设置超时时间和编码方式
     */
    private Site site = Site.me().setTimeOut(5000).setCharset("utf8").setSleepTime(100);

    @Override
    public Site getSite() {
        return site;
    }
}
