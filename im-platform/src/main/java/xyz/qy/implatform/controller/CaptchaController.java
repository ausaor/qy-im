package xyz.qy.implatform.controller;

import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.enums.ResultCode;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.util.Base64;
import xyz.qy.implatform.util.IdUtils;
import xyz.qy.implatform.util.RandImageUtil;
import xyz.qy.implatform.util.RedisCache;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 验证码操作处理
 * 
 * @author ruoyi
 */
@RestController
public class CaptchaController {

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Autowired
    private RedisCache redisCache;
    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public Result getCode() throws IOException {
        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = RedisKey.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 生成验证码
        capStr = code = captchaProducer.createText();
        image = RandImageUtil.getImageBuffer(capStr);
        Map<String, String> reault = new HashMap<>();
        redisCache.setCacheObject(verifyKey, code, RedisKey.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return ResultUtils.error(ResultCode.PROGRAM_ERROR);
        }

        reault.put("uuid", uuid);
        reault.put("img", Base64.encode(os.toByteArray()));
        return ResultUtils.success(reault);
    }
}
