package xyz.qy.implatform.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
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
    // 私钥
    private static final String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCDXS+8l0PefoRrBlk1Eq1DS5RNa+YjSQbahGyLQBfxhk9P6ue819i7P+Jn5hp2GpBU5e7Y6bEhAfxlQsRB6DBNSj/jHhg3+ldP2kPNfLmdzFyQClGNbwjzgEY5qByRwRQEiOZT89NoYcTZqbhXzpVVmIfYl0wr6EXgSRmAwvQ9t4UHuysrEulTjV22vZWScZq3xlSpXK0O+UgYrJ+QeD9oMlCvxEIwgbAx4hl7q2l/fEff/0LO/ATWH5zCe419LF6whXFoCYt5BTL5yoFL77cBvzQLhP6OuVxdeAKNgC+0SYFvtGvJFiqKllkLMOqAzLHkAKxtgFC3vo2v5L2yZcQbAgMBAAECggEAZTMM64S8zFM7QBgL8U8KG6XGAj71dIl1tjH+TOh7iZ2mzDJzhI/kb5yvlss153ZfaogYkRK3Wm5lOr1M5FkznNdukFFG5xj82o+TXGKRvHcLeAv697Jqeux/LTvNhpvwB+5PQq28V9W6CNUvJrYHZYG0Za9pBwpRio/GDeQitMfmD5ywcj+Nmvlea+Q/1OZHNIYVn9BJUkprojWZLvR8NSK0ZKPDp6ItEwK3y+qu4xo/fdB6Rc4gMfpdRsaiH0tLxHi5gYC+pbYBwKw7njRLe+zmpQ5kI9KER//BEMnKFItfpUKvXYUIikVVNJ5hyEopfu56DGR1yfhX5cHcYtTWAQKBgQDZgHIz6b5AHTb7MsxYQxeD+VUDILXD7lvGbwvPjOR8NilCByJGYHBengRNcNIq9LQAJl1rHj3xQJGfbvNY67DsGiwmWmBw7/bv5ca6wIaxTVbDcEWPuuOMQkqQ0TkSHCmLudjaXuDD8wNIUXmyjEHQWCFQ93bS9Lr7SUM8k/0YIQKBgQCanZ80bmW9PPRf+UfdkqhGjgXacsIK0raV69KRR18ig0pwVyOA2IQXV4IRQGvUJ1qfTX0M61ClC+hewWZ99ZEiRYW4mw46q4L3t3+OME3BMNtN6gnNFlOaBX7uoSqgBCjxnDWYy+e6+Avy8QblAibsDAO9ANB81jXSQiejkxCkuwKBgAhiGQTDN6AFAD+CX7nvLBLcI1ixhA//Oe5WDWB8j7nx5Hk1CS8pPGuJL6VEefVJ5T40PFmKmtyXnoVEo4CD5RfUkmsQBLOVrDDm7Ff6X7apwnRZ+XRmwaqAS5t1VPvLthY6nY8dJRCoZzH8YfJt0YOUQgR65UuQPzy8QCkvkwvBAoGAMgW640iwS8gzu/U3hf0TyjfatvEXzE6d2eVA69WudskJRU075Eyod7DNGO3BtRLCY/W69noPTkaK+FUT5r6TJQZfG/m2Yc7pgWFxuDvyfAVlflCwuRR3GcabaXLlCn4uDPUf9f5KS5hCrWfDdkX697VvZC+mRC09nnfewTIsWU8CgYAaICvKKfVELhg1kAr8Ky9PTPZMLXtq0waWgsczhzE1bXjAdUQTnRo2qqb7GjGWykKAWeKaf/3eu1XU0MT1tQCWUw5MJRkcEZ14cipSJA40Kdn0ISim7dgxlytpPRlj3Oa/ogMMq3AELsqiUWf4hx048VtQMJxoYx9INwXIBRioqg==";

    // 公钥
    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg10vvJdD3n6EawZZNRKtQ0uUTWvmI0kG2oRsi0AX8YZPT+rnvNfYuz/iZ+YadhqQVOXu2OmxIQH8ZULEQegwTUo/4x4YN/pXT9pDzXy5ncxckApRjW8I84BGOagckcEUBIjmU/PTaGHE2am4V86VVZiH2JdMK+hF4EkZgML0PbeFB7srKxLpU41dtr2VknGat8ZUqVytDvlIGKyfkHg/aDJQr8RCMIGwMeIZe6tpf3xH3/9CzvwE1h+cwnuNfSxesIVxaAmLeQUy+cqBS++3Ab80C4T+jrlcXXgCjYAvtEmBb7RryRYqipZZCzDqgMyx5ACsbYBQt76Nr+S9smXEGwIDAQAB";

    /**
     * 生成RSA密钥对
     */
    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 生成私钥（后端用）
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            // 生成公钥（前端用）
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            System.out.println("私钥：\n" + privateKey);
            System.out.println("公钥：\n" + publicKey);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成密钥对失败：{}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        generateKeyPair();
    }

    // 使用公钥加密
    public static String encrypt(String data) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("RSA加密失败：{}", e.getMessage());
        }
        return "";
    }

    // 使用私钥解密
    public static String decrypt(String encryptedData) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(PRIVATE_KEY);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            log.error("解密失败：{}", e.getMessage());
        }
        return "";
    }
}

