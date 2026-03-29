package xyz.qy.implatform.service.impl;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.dto.EmailDTO;
import xyz.qy.implatform.entity.User;
import xyz.qy.implatform.enums.EmailCategoryEnum;
import xyz.qy.implatform.event.publisher.SendMailEventPublisher;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.service.IUserService;
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

    @Resource
    private IUserService userService;

    public void getEmailCode(EmailDTO emailDTO) {
        if (redisCache.hasKey(RedisKey.IM_CACHE_MAIL_CODE + emailDTO.getCategory() + ":" + emailDTO.getToEmail())) {
            throw new GlobalException("请勿重复发送验证码");
        }
        checkUserExist(emailDTO);

        String code = captchaProducer.createText();
        redisCache.setCacheObject(RedisKey.IM_CACHE_MAIL_CODE + emailDTO.getCategory() + ":"  + emailDTO.getToEmail(), code, RedisKey.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        publisher.sendMailAsync("验证码", code, emailDTO.getToEmail());
        log.info("绑定邮箱验证码推送事件成功:{}", emailDTO.getToEmail());
    }

    private void checkUserExist(EmailDTO emailDTO) {
        if (EmailCategoryEnum.RESET_PASSWORD.name().equals(emailDTO.getCategory())
            || EmailCategoryEnum.LOGIN.name().equals(emailDTO.getCategory())) {
            Integer count = userService.lambdaQuery()
                    .eq(User::getEmail, emailDTO.getToEmail())
                    .eq(User::getIsDisable, false)
                    .count();
            if (count == 0) {
                throw new GlobalException("用户不存在");
            }
        }
    }
}
