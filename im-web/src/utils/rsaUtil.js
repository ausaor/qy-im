import JSEncrypt from 'jsencrypt'

// 使用代码中提供的公钥
const PUBLIC_KEY = `-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg10vvJdD3n6EawZZNRKtQ0uUTWvmI0kG2oRsi0AX8YZPT+rnvNfYuz/iZ+YadhqQVOXu2OmxIQH8ZULEQegwTUo/4x4YN/pXT9pDzXy5ncxckApRjW8I84BGOagckcEUBIjmU/PTaGHE2am4V86VVZiH2JdMK+hF4EkZgML0PbeFB7srKxLpU41dtr2VknGat8ZUqVytDvlIGKyfkHg/aDJQr8RCMIGwMeIZe6tpf3xH3/9CzvwE1h+cwnuNfSxesIVxaAmLeQUy+cqBS++3Ab80C4T+jrlcXXgCjYAvtEmBb7RryRYqipZZCzDqgMyx5ACsbYBQt76Nr+S9smXEGwIDAQAB
-----END PUBLIC KEY-----`;

/**
 * 使用公钥加密数据
 * @param {string} data - 需要加密的明文数据
 * @returns {string|null} 经过Base64编码的加密数据
 */
export const encryptByPublicKey = (data) => {
  try {
    const encrypt = new JSEncrypt()
    encrypt.setPublicKey(PUBLIC_KEY)
    return encrypt.encrypt(data)
  } catch (error) {
    console.error('加密失败:', error)
    return null
  }
}