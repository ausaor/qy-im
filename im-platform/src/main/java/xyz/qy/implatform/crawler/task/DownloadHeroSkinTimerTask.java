package xyz.qy.implatform.crawler.task;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import xyz.qy.implatform.entity.HeroSkin;
import xyz.qy.implatform.service.IHeroSkinService;
import xyz.qy.implatform.util.ImageUtils;
import xyz.qy.implatform.util.SpringUtils;

import java.util.TimerTask;

@Slf4j
@Data
public class DownloadHeroSkinTimerTask extends TimerTask {

    private HeroSkin heroSkin;

    /**
     * 皮肤图片存放目录
     */
    private static String skinPath = "E:/image/wzry/skins/";

    /**
     * 皮肤头像存放目录
     */
    private String skinAvatarPath;

    @Override
    public void run() {

//        String skinFlag = ImageUtils.downloadImage(heroSkin.getSkinUrl(),
//                skinPath + heroSkin.getHeroName() + "/",
//                heroSkin.getSkinName());
        if (StringUtils.isBlank(skinAvatarPath) || ObjectUtil.isNull(heroSkin)) {
            return;
        }

        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
            log.error("error", e);
        }

        String skinFlag = ImageUtils.downloadImage(heroSkin.getSkinProfileUrl(),
                skinAvatarPath + heroSkin.getHeroName() + "/",
                heroSkin.getSkinName());

        if ("success".equals(skinFlag) || "exist".equals(skinFlag)) {
            IHeroSkinService heroSkinService = SpringUtils.getBean(IHeroSkinService.class);
            heroSkin.setIsCrawl(1);
            heroSkinService.updateById(heroSkin);
        }
    }
}

