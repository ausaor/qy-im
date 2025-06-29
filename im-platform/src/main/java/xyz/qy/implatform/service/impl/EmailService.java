package xyz.qy.implatform.service.impl;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.EmailDTO;
import xyz.qy.implatform.event.publisher.SendMailEventPublisher;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailService {
    @Resource
    private SendMailEventPublisher publisher;

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource
    private RedisCache redisCache;

    public void getEmailCode(EmailDTO emailDTO) {
        if (redisCache.hasKey(RedisKey.IM_CACHE_MAIL_BIND + emailDTO.getCategory() + ":" + emailDTO.getToEmail())) {
            throw new GlobalException("请勿重复发送验证码");
        }

        String code = captchaProducer.createText();
        redisCache.setCacheObject(RedisKey.IM_CACHE_MAIL_BIND + emailDTO.getCategory() + ":"  + emailDTO.getToEmail(), code, RedisKey.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        publisher.sendMailAsync("验证码", "您的验证码是：" + code + "，有效时间5分钟", emailDTO.getToEmail());
        log.info("绑定邮箱验证码推送事件成功:{}", emailDTO.getToEmail());
    }
}
