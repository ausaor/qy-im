package xyz.qy.implatform.service;

import org.junit.jupiter.api.Test;
import xyz.qy.implatform.util.SysStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Polaris
 * @create: 2023-06-10 09:14
 **/
@Slf4j
@SpringBootTest
public class UserServiceTest {
    @Resource
    private IUserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Test
    public void generateRandomUsernameTest() {
        String username = userService.generateRandomUsername();
        log.info("username=" + username);
    }

    @Test
    public void makeRandomPassword() {
        String password = SysStringUtils.makeRandomPassword();
        String encode = passwordEncoder.encode(password);
        log.info("encode=" + encode);
    }
}
