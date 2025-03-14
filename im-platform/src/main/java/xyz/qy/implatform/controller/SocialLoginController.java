package xyz.qy.implatform.controller;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.enums.LoginTypeEnum;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.strategy.context.SocialLoginStrategyContext;
import xyz.qy.implatform.vo.LoginVO;
import xyz.qy.implatform.vo.QQLoginVO;

import javax.validation.Valid;

/**
 * 登录处理
 */
@RestController
@RequestMapping("/social/login")
public class SocialLoginController {

    @Autowired
    private SocialLoginStrategyContext socialLoginStrategyContext;

    /**
     * qq登录
     *
     * @param qqLoginVO qq登录信息
     * @return 用户信息
     */
    @ApiOperation(value = "qq登录")
    @PostMapping("/qq")
    public Result<LoginVO> qqLogin(@Valid @RequestBody QQLoginVO qqLoginVO) {
        return ResultUtils.success(socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(qqLoginVO), LoginTypeEnum.QQ));
    }
}
