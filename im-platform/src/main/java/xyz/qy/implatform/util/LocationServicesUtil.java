package xyz.qy.implatform.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import xyz.qy.implatform.vo.IpGeoInfoVO;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 位置服务工具类
 *
 * @author Polaris
 * @since 2024-10-18
 */
@Slf4j
@Component
public class LocationServicesUtil {
    /**
     * 参与加密路径
     */
    private static final String ENCRYPTION_PATH = "/ws/location/v1/ip?ip=%s&key=%s%s";

    /**
     * 腾讯位置服务
     */
    private static final String TENCENT_LOCATION_SERVICE = "https://apis.map.qq.com/ws/location/v1/ip?ip=%s&key=%s&sig=%s";

    @Value("${tencent.location.key}")
    private String key;

    @Value("${tencent.location.sk}")
    private String sk;

    @Value("${tianditu.map.key}")
    private String tianDiTuKey;

    @Value("${tianditu.map.url}")
    private String tianDiTuUrl;

    @Value("${gaode.map.key}")
    private String gaoDeMapKey;

    @Value("${gaode.map.url}")
    private String gaoDeMapUrl;


    /**
     * 查询IP属地信息
     *
     * @param ipAddress IP地址
     * @return IpGeoInfoVO
     */
    public IpGeoInfoVO getIpGeoInfoByTencentApi(String ipAddress) {
        IpGeoInfoVO ipGeoInfoVO = null;
        try {
            String url = String.format(TENCENT_LOCATION_SERVICE, ipAddress, key, getSign(ipAddress, key, sk));
            String result = HttpUtil.get(url);
            log.info("tencent location result:{}", result);
            JSONObject jsonObject = JSONObject.parseObject(result);

            if (CollectionUtil.isNotEmpty(jsonObject) &&
                    jsonObject.getInteger("status").equals(0)) {
                JSONObject resultJson = jsonObject.getJSONObject("result");
                if (CollectionUtil.isEmpty(resultJson)) {
                    return null;
                }
                JSONObject adInfo = resultJson.getJSONObject("ad_info");
                if (CollectionUtil.isEmpty(adInfo)) {
                    return null;
                }
                ipGeoInfoVO = new IpGeoInfoVO();
                String nation = adInfo.getString("nation");
                String province = adInfo.getString("province");
                String city = adInfo.getString("city");
                ipGeoInfoVO.setIp(ipAddress);
                ipGeoInfoVO.setNation(nation);
                ipGeoInfoVO.setPro(province);
                ipGeoInfoVO.setCity(city);
                ipGeoInfoVO.setLocationInfo(resultJson.toJSONString());
            }
        } catch (Exception e) {
            log.error("getIpGeoInfoByTencentApi error", e);
        }
        return ipGeoInfoVO;
    }

    /**
     * 获取加密内容签名
     *
     * @param ip  ip
     * @param key key
     * @param sk  密钥
     * @return 签名
     */
    public String getSign(String ip, String key, String sk) {
        return DigestUtils.md5DigestAsHex(String.format(ENCRYPTION_PATH, ip, key, sk).getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, String> getLngAndLatByAddr(String address) {
        //return getLngAndLatWithAddrByTianDiTu(address);
        return getLngAndLatWithAddrByGaoDeApi(address);
    }

    public Map<String, String> getLngAndLatWithAddrByTianDiTu(String address) {
        Map<String, String> map = new HashMap<>();
        try {
            String requestUrl = String.format(tianDiTuUrl, address, tianDiTuKey);
            log.info("requestUrl:{}", requestUrl);
            String result = HttpUtil.get(requestUrl);
            JSONObject jsonObject = JSON.parseObject(result);
            log.info("jsonObject:{}", jsonObject);
            if ("0".equals(jsonObject.getString("status"))) {
                JSONObject location = jsonObject.getJSONObject("location");
                map.put("lng", location.getString("lon"));
                map.put("lat", location.getString("lat"));
            }
        } catch (Exception e) {
            log.error("getLngAndLatWithAddrByTianDiTu() error", e);
        }
        return map;
    }

    public Map<String, String> getLngAndLatWithAddrByGaoDeApi(String address) {
        Map<String, String> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        builder.append(gaoDeMapUrl)
                .append("key=").append(gaoDeMapKey).append("&")
                .append("address=").append(address);
        String requestUrl = builder.toString();
        log.info("requestUrl:{}", requestUrl);
        String result = HttpUtil.get(requestUrl);
        JSONObject jsonObject = JSON.parseObject(result);
        log.info("jsonObject:{}", jsonObject);
        if ("1".equals(jsonObject.getString("status"))) {
            JSONArray geocodes = jsonObject.getJSONArray("geocodes");
            if (CollectionUtil.isNotEmpty(geocodes)) {
                JSONObject object = JSONObject.parseObject(geocodes.get(0).toString());
                String location = object.getString("location");
                if (StringUtils.isNoneBlank(location)) {
                    String[] arr = location.split(",");
                    if (arr.length >= 2) {
                        map.put("lng", arr[0]);
                        map.put("lat", arr[1]);
                    }
                }
            }
        }
        return map;
    }
}
