package xyz.qy.implatform.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

@Slf4j
public class ImageUtils {

    public static final String PIC_URL = "https://unsplash.it/800/450?image=";

    /**
     * 获取随机图片链接
     * @return
     */
    public static String getRandomImage(){
        int max = 1000;
        int min = 1;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return PIC_URL + s;
    }

    /**
     * 下载图片
     *
     * @param url 图片url
     * @param directory 存放目录
     * @param name 文件名
     */
    public static String downloadImage(String url, String directory, String name) {
        InputStream inputStream = null;
        FileOutputStream os = null;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {    // 目录不存在则创建目录
                dir.mkdirs();
            }
            String realExt = url.substring(url.lastIndexOf("."));   // 获取扩展名
            String fileName = name + realExt;
            //fileName = fileName.replace("-", "");
            String filePath = directory + fileName;
            File img = new File(filePath);
            if(img.exists()){   // 若文件之前已经下载过，则跳过
                log.info(String.format("文件%s已存在本地目录", fileName));
                return "exist";
            }

            URLConnection con = new URL(url).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            inputStream = con.getInputStream();
            byte[] bs = new byte[1024];

            File file = new File(filePath);
            os = new FileOutputStream(file, true);
            // 开始读取 写入
            int len;
            while ((len = inputStream.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            return "success";
        } catch (Exception e) {
            log.error("error", e);
            return "fail";
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("close error", e);
            }
        }
    }
}
