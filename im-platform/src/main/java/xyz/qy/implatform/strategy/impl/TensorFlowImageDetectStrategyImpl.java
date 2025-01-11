//package xyz.qy.implatform.strategy.impl;
//
//import cn.hutool.core.io.FileUtil;
//import cn.hutool.http.HttpUtil;
//import com.ruibty.nsfw.NsfwService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// * TensorFlow实现图片检测
// *
// * @author Polaris
// * @since 2024-10-15
// */
//@Slf4j
//@Service("tensorFlowImageDetectStrategyImpl")
//public class TensorFlowImageDetectStrategyImpl extends AbstractImageDetectStrategyImpl {
//    @Autowired
//    private NsfwService nsfwService;
//
//    @Override
//    public boolean doImageDetect(String filePath) {
//        byte[] bytes = FileUtil.readBytes(filePath);
//
//        float prediction = nsfwService.getPrediction(bytes);
//        log.info("TensorFlow prediction：{}", prediction);
//        return prediction < 0.8f;
//    }
//
//    @Override
//    public boolean doImageDetectByUrl(String imgUrl) {
//        byte[] bytes = HttpUtil.downloadBytes(imgUrl);
//
//        float prediction = nsfwService.getPrediction(bytes);
//        log.info("TensorFlow prediction：{}", prediction);
//        return prediction < 0.8f;
//    }
//}
