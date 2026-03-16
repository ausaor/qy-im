package xyz.qy.implatform.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.imageaudit20191230.Client;
import com.aliyun.imageaudit20191230.models.ScanImageAdvanceRequest;
import com.aliyun.imageaudit20191230.models.ScanImageResponse;
import com.aliyun.imageaudit20191230.models.ScanImageResponseBody.ScanImageResponseBodyData;
import com.aliyun.imageaudit20191230.models.ScanImageResponseBody.ScanImageResponseBodyDataResults;
import com.aliyun.imageaudit20191230.models.ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("aliYunContentDetectStrategyImpl")
public class AliYunContentDetectStrategyImpl extends AbstractImageDetectStrategyImpl {

    @Resource
    private Client client;

    @Override
    public boolean doImageDetect(String filePath) {
        try (InputStream inputStream1 = new FileInputStream(new File(filePath))) {
            ScanImageAdvanceRequest.ScanImageAdvanceRequestTask task0 = new ScanImageAdvanceRequest.ScanImageAdvanceRequestTask();
            task0.setDataId(UUID.randomUUID().toString()); // 数据 ID。需要保证在一次请求中所有的 ID 不重复
            task0.setImageTimeMillisecond(System.currentTimeMillis()); //图片创建或编辑时间，单位为毫秒
            task0.setInterval(1);
            task0.setMaxFrames(1);
            task0.setImageURLObject(inputStream1);
            List<ScanImageAdvanceRequest.ScanImageAdvanceRequestTask> taskList = new ArrayList<>();
            taskList.add(task0);
            List<String> sceneList = new ArrayList<>();
            //指定图片检测的应用场景，可选值包括：
            //porn：图片智能鉴黄
            //terrorism：图片敏感内容识别、图片风险人物识别
            //ad：图片垃圾广告识别
            //live：图片不良场景识别
            //logo：图片 Logo 识别
            sceneList.add("porn");
            sceneList.add("terrorism");
            ScanImageAdvanceRequest scanImageAdvanceRequest = new ScanImageAdvanceRequest()
                    .setTask(taskList)
                    .setScene(sceneList);
            RuntimeOptions runtime = new RuntimeOptions();
            ScanImageResponse scanImageResponse = client.scanImageAdvance(scanImageAdvanceRequest, runtime);
            
            log.info("详细数据：{}", JSON.toJSONString(scanImageResponse.getBody().getData()));

            ScanImageResponseBodyData data = scanImageResponse.getBody().getData();
            List<ScanImageResponseBodyDataResults> results = data.getResults();
            for (ScanImageResponseBodyDataResults result : results) {
                for (ScanImageResponseBodyDataResultsSubResults subResult :result.subResults) {
                    log.info("图片违规场景：{}", subResult.scene);
                    //pass：图片正常，无需进行其余操作；或者未识别出目标对象。
                    //review：检测结果不确定，需要进行人工审核；或者未识别出目标对象。
                    //block：图片违规，建议执行进一步操作（如直接删除或做限制处理）。
                    if ("block".equals(subResult.getSuggestion())) {
                        return true;
                    }
                }
            }

        } catch (TeaException teaException) {
            log.error("阿里云图片检测异常 - 代码：{}, 消息：{}", teaException.getCode(), teaException.getMessage());
        } catch (IOException e) {
            log.error("读取图片文件失败", e);
        } catch (Exception e) {
            log.error("图片检测过程异常", e);
        }
        return false;
    }

    @Override
    public boolean doImageDetectByUrl(String imgUrl) {
        try (InputStream inputStream1 = new URL(imgUrl).openStream()) {
            ScanImageAdvanceRequest.ScanImageAdvanceRequestTask task0 = new ScanImageAdvanceRequest.ScanImageAdvanceRequestTask();
            task0.setDataId(UUID.randomUUID().toString());
            task0.setImageTimeMillisecond(System.currentTimeMillis());
            task0.setInterval(1);
            task0.setMaxFrames(1);
            task0.setImageURLObject(inputStream1);
            List<ScanImageAdvanceRequest.ScanImageAdvanceRequestTask> taskList = new ArrayList<>();
            taskList.add(task0);
            List<String> sceneList = new ArrayList<>();
            sceneList.add("porn");
            ScanImageAdvanceRequest scanImageAdvanceRequest = new ScanImageAdvanceRequest()
                    .setTask(taskList)
                    .setScene(sceneList);
            RuntimeOptions runtime = new RuntimeOptions();
            ScanImageResponse scanImageResponse = client.scanImageAdvance(scanImageAdvanceRequest, runtime);

            log.info("详细数据：{}", JSON.toJSONString(scanImageResponse.getBody().getData()));

            ScanImageResponseBodyData data = scanImageResponse.getBody().getData();
            List<ScanImageResponseBodyDataResults> results = data.getResults();
            for (ScanImageResponseBodyDataResults result : results) {
                for (ScanImageResponseBodyDataResultsSubResults subResult :result.subResults) {
                    log.info("图片违规场景：{}", subResult.scene);
                    if ("block".equals(subResult.getSuggestion())) {
                        return true;
                    }
                }
            }
            
        } catch (TeaException teaException) {
            log.error("阿里云图片检测异常 - 代码：{}, 消息：{}", teaException.getCode(), teaException.getMessage());
        } catch (IOException e) {
            log.error("读取图片链接失败：{}", imgUrl, e);
        } catch (Exception e) {
            log.error("图片链接检测过程异常", e);
        }
        return false;
    }
}
