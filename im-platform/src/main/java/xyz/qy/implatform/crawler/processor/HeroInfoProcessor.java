//package xyz.qy.implatform.crawler.processor;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.edge.EdgeDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.stereotype.Component;
//import us.codecraft.webmagic.Page;
//import us.codecraft.webmagic.Site;
//import us.codecraft.webmagic.processor.PageProcessor;
//import us.codecraft.webmagic.selector.Selectable;
//import xyz.qy.implatform.entity.HeroInfo;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//public class HeroInfoProcessor implements PageProcessor {
//    @Override
//    public void process(Page page) {
//        //System.setProperty("webdriver.edge.driver", "D:\\Java\\software\\edgedriver\\edgedriver_arm64_131\\msedgedriver.exe");
//
//        //创建火狐浏览器驱动
//        /*System.getProperties().setProperty("webdriver.firefox.bin", "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
//        System.getProperties().setProperty("webdriver.gecko.driver", "D:\\Java\\software\\FirefoxDriver\\geckodriver-v0.33.0-win64\\geckodriver.exe");
//        WebDriver driver = new FirefoxDriver();*/
//
//        //创建谷歌浏览器驱动
//        //System.getProperties().setProperty("webdriver.chrome.whitelistedIps", "");
////        System.getProperties().setProperty("webdriver.chrome.driver", "C:/Program Files/Java/jdk1.8.0_301/bin/chromedriver.exe");
//        System.getProperties().setProperty("webdriver.chrome.driver", "D:/Java/software/09_chromedriver/chromedriver-win64_131/chromedriver-win64/chromedriver.exe");
//
//        WebDriver driver = null;
//        try {
//            Selectable selectable = page.getUrl();
//            log.info("page-url:{}", page.getUrl());
//            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--remote-allow-origins=*");//解决403报错问题（允许所有请求，避免在远程调试过程中出现WebSocket连接错误）
//            driver = new ChromeDriver(options);
//
//            //进入页面
//            driver.get(selectable.toString());
//
//            //实际场景中可能会发生页面节点没有加载出来但是程序已经跑完的情况
//            //可以利用显示等待，等待节点加载出来程序继续运行
//            //但是不能无限等待
//            //这里设置等待时长为5秒
//            //超过这个时间就会抛出异常
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
//
//            WebElement ul = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//ul[@class='herolist clearfix']")));
//
//            List<WebElement> lis = ul.findElements(By.cssSelector("ul > li"));
//            if (CollectionUtils.isNotEmpty(lis)) {
//                List<HeroInfo> heroInfoList = new ArrayList<>(lis.size());
//                log.info("共有英雄" + lis.size() + "位");
//                HeroInfo heroInfo = null;
//                for (WebElement li : lis) {
//                    try {
//                        WebElement a = li.findElement(By.tagName("a"));
//                        String heroDetailUrl = a.getAttribute("href");
//                        String[] arr = heroDetailUrl.split("/");
//                        String lastStr = arr[arr.length - 1];
//                        String[] arr2 = lastStr.split("\\.");
//                        String heroId = arr2[0];
//                        String heroName = a.getText();
//                        WebElement img = a.findElement(By.tagName("img"));
//                        String profileUrl = img.getAttribute("src");
//                        log.info("英雄名称：" + heroName);
//                        log.info("英雄详情链接：" + heroDetailUrl);
//                        log.info("英雄头像链接：" + profileUrl);
//                        heroInfo = new HeroInfo();
//                        heroInfo.setHeroName(heroName);
//                        heroInfo.setHeroDetailUrl(heroDetailUrl);
//                        heroInfo.setHeroProfileUrl(profileUrl);
//                        heroInfo.setHeroId(heroId);
//                        heroInfo.setCategory("wzry");
//                        heroInfoList.add(heroInfo);
//                    } catch (Exception e) {
//                        log.error("error:", e);
//                    }
//                }
//                if (CollectionUtils.isNotEmpty(heroInfoList)) {
//                    page.putField("heroInfoList", heroInfoList);
//                }
//            }
//        } catch (Exception e) {
//            log.error("error", e);
//        } finally {
//            assert driver != null;
//            driver.quit();
//        }
//    }
//
//    /**
//     * 对爬虫进行配置
//     * 设置超时时间和编码方式
//     */
//    private Site site = Site.me().setTimeOut(5000).setCharset("utf8").setSleepTime(100);
//
//    @Override
//    public Site getSite() {
//        return site;
//    }
//}
