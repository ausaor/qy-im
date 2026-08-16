package xyz.qy.implatform.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xyz.qy.implatform.entity.DictData;
import xyz.qy.implatform.mapper.DictDataMapper;
import xyz.qy.implatform.service.IDictDataService;

@Service
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements IDictDataService {
    @Override
    public JSONObject getIcpInfo() {
        DictData dictData = this.lambdaQuery()
                .eq(DictData::getDictType, "beian")
                .last("limit 1")
                .one();
        if (dictData == null) {
            return JSONObject.parseObject("{}");
        }

        return JSONObject.parseObject(dictData.getDictValue());
    }
}
