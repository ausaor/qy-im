package xyz.qy.implatform.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.qy.implatform.result.Result;
import xyz.qy.implatform.result.ResultUtils;
import xyz.qy.implatform.util.RedisCache;

import javax.annotation.Resource;

@RequestMapping("/openApi/redis")
@RestController
public class RedisOperateContoller {
    @Resource
    private RedisCache redisCache;

    @PostMapping("/recordKeyValueScore")
    public Result zsetOperate(@RequestBody JSONObject jsonObject) {
        String key = jsonObject.getString("key");
        String value = jsonObject.getString("value");
        Double score = jsonObject.getDouble("score");
        Double resultScore = redisCache.recordKeyValueScore(key, value, score);
        System.out.println(resultScore);
        return ResultUtils.success();
    }
}
