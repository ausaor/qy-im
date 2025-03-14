package xyz.qy.implatform.strategy.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.contant.RedisKey;
import xyz.qy.implatform.exception.GlobalException;
import xyz.qy.implatform.util.HttpUtil;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 百度图片检测实现
 *
 * @author Polaris
 * @since 2024-10-13
 */
@Slf4j
@Service("baiduImageDetectStrategyImpl")
public class BaiduImageDetectStrategyImpl extends AbstractImageDetectStrategyImpl{
    @Resource
    private RedisCache redisCache;

    @Value("${image.detect.baidu.detect-url}")
    private String baiduImageDetectUrl;

    @Value("${image.detect.baidu.access-token-url}")
    private String baiduTokenUrl;

    @Override
    public boolean doImageDetect(String filePath) {
        byte[] bytes = FileUtil.readBytes(filePath);
        String imgStr = Base64.encode(bytes);

        String accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            return false;
        }

        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> contentMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        contentMap.put("image", imgStr);
        String url = baiduImageDetectUrl + accessToken;
        String result = HttpUtil.postMap(url, headerMap, contentMap);
        log.info("baiduImageDetectResult:{}", result);
        if (StringUtils.isBlank(result)) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(result);
        // 审核结果类型，可取值1、2、3、4，分别代表1：合规，2：不合规，3：疑似，4：审核失败
        String conclusionType = jsonObject.getString("conclusionType");
        if ("1".equals(conclusionType)) {
            return true;
        } else if ("4".equals(conclusionType)) {
            throw new GlobalException("图片检测失败");
        }
        return false;
    }

    @Override
    public boolean doImageDetectByUrl(String imgUrl) {
        String accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            return false;
        }

        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> contentMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        contentMap.put("imgUrl", imgUrl);
        String url = baiduImageDetectUrl + accessToken;
        String result = HttpUtil.postMap(url, headerMap, contentMap);
        log.info("baiduImageDetectResult:{}", result);
        if (StringUtils.isBlank(result)) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(result);
        // 审核结果类型，可取值1、2、3、4，分别代表1：合规，2：不合规，3：疑似，4：审核失败
        String conclusionType = jsonObject.getString("conclusionType");
        if ("1".equals(conclusionType)) {
            return true;
        } else if ("4".equals(conclusionType)) {
            throw new GlobalException("图片检测失败");
        }
        return false;
    }

    private String getAccessToken() {
        Object token = redisCache.getCacheObject(RedisKey.IM_BAIDU_TOKEN);
        if (ObjectUtil.isNotNull(token)) {
            return token.toString();
        }
        String result = cn.hutool.http.HttpUtil.post(baiduTokenUrl, "");
        log.info("getBaiduAccessTokenResult:{}", result);
        if (StringUtils.isBlank(result)) {
            return null;
        }

        JSONObject jsonObject = JSON.parseObject(result);
        String accessToken = jsonObject.getString("access_token");
        Integer timeout = jsonObject.getInteger("expires_in");
        if (StringUtils.isNotBlank(accessToken)) {
            redisCache.setCacheObject(RedisKey.IM_BAIDU_TOKEN, accessToken, timeout, TimeUnit.SECONDS);
        }
        return accessToken;
    }
}
