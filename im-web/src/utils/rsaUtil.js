import JSEncrypt from 'jsencrypt'

// 使用代码中提供的私钥
const PRIVATE_KEY = `-----BEGIN PUBLIC KEY-----
MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCDXS+8l0PefoRrBlk1Eq1DS5RNa+YjSQbahGyLQBfxhk9P6ue819i7P+Jn5hp2GpBU5e7Y6bEhAfxlQsRB6DBNSj/jHhg3+ldP2kPNfLmdzFyQClGNbwjzgEY5qByRwRQEiOZT89NoYcTZqbhXzpVVmIfYl0wr6EXgSRmAwvQ9t4UHuysrEulTjV22vZWScZq3xlSpXK0O+UgYrJ+QeD9oMlCvxEIwgbAx4hl7q2l/fEff/0LO/ATWH5zCe419LF6whXFoCYt5BTL5yoFL77cBvzQLhP6OuVxdeAKNgC+0SYFvtGvJFiqKllkLMOqAzLHkAKxtgFC3vo2v5L2yZcQbAgMBAAECggEAZTMM64S8zFM7QBgL8U8KG6XGAj71dIl1tjH+TOh7iZ2mzDJzhI/kb5yvlss153ZfaogYkRK3Wm5lOr1M5FkznNdukFFG5xj82o+TXGKRvHcLeAv697Jqeux/LTvNhpvwB+5PQq28V9W6CNUvJrYHZYG0Za9pBwpRio/GDeQitMfmD5ywcj+Nmvlea+Q/1OZHNIYVn9BJUkprojWZLvR8NSK0ZKPDp6ItEwK3y+qu4xo/fdB6Rc4gMfpdRsaiH0tLxHi5gYC+pbYBwKw7njRLe+zmpQ5kI9KER//BEMnKFItfpUKvXYUIikVVNJ5hyEopfu56DGR1yfhX5cHcYtTWAQKBgQDZgHIz6b5AHTb7MsxYQxeD+VUDILXD7lvGbwvPjOR8NilCByJGYHBengRNcNIq9LQAJl1rHj3xQJGfbvNY67DsGiwmWmBw7/bv5ca6wIaxTVbDcEWPuuOMQkqQ0TkSHCmLudjaXuDD8wNIUXmyjEHQWCFQ93bS9Lr7SUM8k/0YIQKBgQCanZ80bmW9PPRf+UfdkqhGjgXacsIK0raV69KRR18ig0pwVyOA2IQXV4IRQGvUJ1qfTX0M61ClC+hewWZ99ZEiRYW4mw46q4L3t3+OME3BMNtN6gnNFlOaBX7uoSqgBCjxnDWYy+e6+Avy8QblAibsDAO9ANB81jXSQiejkxCkuwKBgAhiGQTDN6AFAD+CX7nvLBLcI1ixhA//Oe5WDWB8j7nx5Hk1CS8pPGuJL6VEefVJ5T40PFmKmtyXnoVEo4CD5RfUkmsQBLOVrDDm7Ff6X7apwnRZ+XRmwaqAS5t1VPvLthY6nY8dJRCoZzH8YfJt0YOUQgR65UuQPzy8QCkvkwvBAoGAMgW640iwS8gzu/U3hf0TyjfatvEXzE6d2eVA69WudskJRU075Eyod7DNGO3BtRLCY/W69noPTkaK+FUT5r6TJQZfG/m2Yc7pgWFxuDvyfAVlflCwuRR3GcabaXLlCn4uDPUf9f5KS5hCrWfDdkX697VvZC+mRC09nnfewTIsWU8CgYAaICvKKfVELhg1kAr8Ky9PTPZMLXtq0waWgsczhzE1bXjAdUQTnRo2qqb7GjGWykKAWeKaf/3eu1XU0MT1tQCWUw5MJRkcEZ14cipSJA40Kdn0ISim7dgxlytpPRlj3Oa/ogMMq3AELsqiUWf4hx048VtQMJxoYx9INwXIBRioqg==
-----END PUBLIC KEY-----`;
/**
 * 使用公钥解密由私钥加密的数据
 * @param {string} encryptedData - 经过Base64编码的加密数据
 * @returns {string|null} 解密后的明文
 */
export const decryptByPublicKey = (encryptedData) => {
  try {
    const decrypt = new JSEncrypt()
    decrypt.setPrivateKey(PRIVATE_KEY)
    //console.log('decryptByPublicKey', decrypt.decrypt(encryptedData))
    return decrypt.decrypt(encryptedData)
  } catch (error) {
    console.error('解密失败:', error)
    return null
  }
}