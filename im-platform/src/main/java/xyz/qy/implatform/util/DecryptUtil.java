package xyz.qy.implatform.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class DecryptUtil {
    // 密钥（与前端完全一致）
    private static final String SECRET_KEY = "ckAmMBhYUZFwrZMMvU6SGYKzSGdQms5Z"; // 32字符（256位）

    /**
     * AES-CBC解密
     * @param encryptedText 前端传来的加密字符串（格式："密文Base64:IVBase64"）
     * @return 解密后的富文本
     * @throws Exception 加密相关异常
     */
    public static String decrypt(String encryptedText) throws Exception {
        // 拆分密文和IV（前端格式：密文:IV）
        String[] parts = encryptedText.split(":", 2);
        String cipherTextBase64 = parts[0];
        String ivBase64 = parts[1];

        // Base64解码：密文->字节数组，IV->字节数组
        byte[] cipherText = Base64.getDecoder().decode(cipherTextBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);

        // 密钥转字节数组（UTF-8编码，与前端一致）
        byte[] keyBytes = SECRET_KEY.getBytes("UTF-8");

        // 初始化AES解密器
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 对应前端PKCS7
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // 解密并转UTF-8字符串
        byte[] plainTextBytes = cipher.doFinal(cipherText);
        return new String(plainTextBytes, "UTF-8");
    }
}
