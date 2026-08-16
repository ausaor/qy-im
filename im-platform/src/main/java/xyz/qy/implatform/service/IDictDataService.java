package xyz.qy.implatform.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.qy.implatform.entity.DictData;

public interface IDictDataService extends IService<DictData> {
    JSONObject getIcpInfo();
}
