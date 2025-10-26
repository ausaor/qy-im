import CryptoJS from 'crypto-js';

// 密钥（需与后端一致，建议后端动态下发，避免硬编码）
const SECRET_KEY = 'ckAmMBhYUZFwrZMMvU6SGYKzSGdQms5Z'; // 注意：256位密钥需32个字符（每个字符8位）

/**
 * 生成随机IV（16字节，AES-CBC要求IV长度为16字节）
 */
const generateIV = () => {
    const iv = CryptoJS.lib.WordArray.random(16); // 16字节随机数
    return CryptoJS.enc.Base64.stringify(iv); // 转Base64便于传输
};

/**
 * AES-CBC加密
 * @param {string} plainText 待加密的富文本内容
 * @returns {string} 加密后格式："密文Base64:IVBase64"
 */
export const encrypt = (plainText) => {
    const ivBase64 = generateIV();
    const iv = CryptoJS.enc.Base64.parse(ivBase64); // IV转字节数组
    const key = CryptoJS.enc.Utf8.parse(SECRET_KEY); // 密钥转字节数组（UTF-8编码）

    // 加密（PKCS7填充，CBC模式）
    const encrypted = CryptoJS.AES.encrypt(
        CryptoJS.enc.Utf8.parse(plainText), // 明文转UTF-8字节
        key,
        {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        }
    );

    // 密文转Base64，与IV拼接（格式：密文:IV）
    return `${encrypted.ciphertext.toString(CryptoJS.enc.Base64)}:${ivBase64}`;
};