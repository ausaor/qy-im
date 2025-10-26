package xyz.qy.implatform.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class RSAUtil {
    // 密钥长度，非对称加密一般使用2048位
    private static final int KEY_SIZE = 2048;
    // 算法名称
    private static final String ALGORITHM = "RSA";
    // 私钥（实际项目中应妥善保管，可配置在环境变量或配置中心）
    private static final String PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCweaINrmD/HWySGQ0QmPywcv+LpuOE1Qq/gS4tYrBD8pPB1SUfOrpe5SnGV9vmIk+Zb3kw0gk+2uIOUpoELj3quNwlsUBg9OErlbnLwfJACWqLOa2pH+kx8nYk7Bk5vaVLfarVO16hMobYwRRpvcDoTTYCZTUSrU+wYAUPSkM/Y0bW3ChvkM9Hl0BB8n+2nNIS0UPMmnrzHnjRbjPvX31Qm0pSqPNSr8Gbtl2Bm1q3GYKz5LE2g0p9JNkHgB6v+igqeYGJIAqwIxnwQ/M2yMZP7f8FUsSoun8DsrnVqhfzY3M7afNuREExLmUigk0txkrbU9ha8oHU+jVG24ijH/chAgMBAAECggEBAIVsYL4xQfp/t485y2vqDtolB9oA4nGSVN72/v+nZpbotxfMW5Wh5DhM0Gm6EXKMLFxYMGtd0UmpWVH016cjgwKMoz0IJyFm4zr9p4xK5SnPNLNwd0Cv9LIjm3lM28GRBYDu0RMIIQKE3fjQ+er8YPYBMj1bYWXujJpP7mNiBTfNR+i37tXiAhzCXkEaWRN6HUAzCSOAWqsiPzAG0Pe6Rdt1XL9U4djUDNYY28drCaRG887J8N/PKOZ/FZt4kIH87Y4geZ5TQcwzOCt6FHfBLrcLQiU10U7fGB1nTjHgqB+TD9CMKilJq9Tu2X82mioaMzYD2qnoFdZEHfldYm94v4UCgYEA5cIVcciQamAypgJd3Z1W1vU+la1kid5suwIFaZIyND/07WIqo4ukf0dLou3umDcUhFoUwmh21dC69MJLCbh3QEFRui55f4nPkIBzj9AgxElD3CcSrswf5ivYkWPLQS9yoKlpMYbhPc4oIoqq2KJ0ZulOJIAlMrA/yAGSDckLFVMCgYEAxKGaZt7hEFvZ09fc9FIpboKNgGETwjOkh15ICMTAI03BDs0ilTUzn1FISKGUQRcuT3kWRH02OVzXxkKFE5b0pAQinl0B1jfUp00ibdag8qogLogsx8EwFRpBoF78v5Y5xI4rYNTVmPeY0FKzniReRz8Tn3/gUYNcJo2I8o1vHzsCgYBkY0/xXlvTTuLybBhNS4D0/UTyK0fBgaDqePCadxlegQ9oZTsSKGKghitDLkjQ00gwqBdn9jLuK3x0kywsPCBGwAOQoNvPYQyDK8zSanLWsNhoqd3c6C2L6V5/8CWP/sZL8pmW3UMvOROKPCJ380t+57OP+TnHkwDbR+xj/5LLJwKBgQCN17W195mLX3bnJpSxeoOAwJSqw1ypfu9050I3cRbZYMGfl8MWt2Yhy1TFcCmuOrahhcvWZCy1fI/66QludDrOBl2it50XlvbEKRRo/6bi5m6IopsLDgVNc79bnKne2tOZQqVwGJhs3E9UGBSSQ9Huu0XAjVKX+paDCR59OiGq+QKBgQCbe2BL8BHM0rTyiBfkfJQQtASKtvtVgy3BdZy01V5x3Z2MjMcvHPQW8xzol0gevLE9gxdblUXruDt4da8ln3RgQowjaXdSsQZttxJRZEBs7nhY8+s3QGNnldOZEyBHWFu37nzaMqsbwZ7XudUndWLXw4LYQP0fjRlvDkEEVJBcrA==";
    // 公钥（可提供给前端）
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsHmiDa5g/x1skhkNEJj8sHL/i6bjhNUKv4EuLWKwQ/KTwdUlHzq6XuUpxlfb5iJPmW95MNIJPtriDlKaBC496rjcJbFAYPThK5W5y8HyQAlqizmtqR/pMfJ2JOwZOb2lS32q1TteoTKG2MEUab3A6E02AmU1Eq1PsGAFD0pDP2NG1twob5DPR5dAQfJ/tpzSEtFDzJp68x540W4z7199UJtKUqjzUq/Bm7ZdgZtatxmCs+SxNoNKfSTZB4Aer/ooKnmBiSAKsCMZ8EPzNsjGT+3/BVLEqLp/A7K51aoX82NzO2nzbkRBMS5lIoJNLcZK21PYWvKB1Po1RtuIox/3IQIDAQAB";

    static {
        // 初始化时生成密钥对（实际项目中只需要生成一次，然后保存密钥）
        // 生成后将公钥和私钥保存，注释掉生成代码
        //generateKeyPair();
    }

    /**
     * 生成RSA密钥对
     */
    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 获取公钥和私钥
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // 转为Base64编码
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            System.out.println("公钥：" + publicKeyStr);
            System.out.println("私钥：" + privateKeyStr);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成密钥对失败：{}", e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        generateKeyPair();
//    }

    /**
     * 使用私钥加密
     */
    public static String encryptByPrivateKey(String data) {
        try {
            // 解码私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(PRIVATE_KEY);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 加密
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);

            // 处理长数据（RSA加密有长度限制）
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);

            // 加密后的数据转为Base64
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("加密失败：{}", e.getMessage());
        }

        return "";
    }
}

