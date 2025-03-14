package xyz.qy.implatform.crawler.task;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.qy.implatform.entity.HeroInfo;
import xyz.qy.implatform.service.IHeroInfoService;
import xyz.qy.implatform.util.ImageUtils;
import xyz.qy.implatform.util.SpringUtils;

import java.util.TimerTask;

@Data
@Slf4j
public class DownloadHeroAvatarTimerTask extends TimerTask {

    private HeroInfo heroInfo;

    private String preFixPath;

    @Override
    public void run() {
        log.info("heroName：{}", heroInfo.getHeroName());
        if (StringUtils.isBlank(preFixPath) || ObjectUtil.isNull(heroInfo)) {
            return;
        }
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
            log.error("error", e);
        }
        String result = ImageUtils.downloadImage(heroInfo.getHeroProfileUrl(), preFixPath, heroInfo.getHeroName());

        if ("success".equals(result) || "exist".equals(result)) {
            IHeroInfoService heroInfoService = SpringUtils.getBean(IHeroInfoService.class);
            heroInfo.setIsCrawl(true);
            heroInfoService.updateById(heroInfo);
        }
    }
}
