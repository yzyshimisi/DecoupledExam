package cn.edu.zjut.backend.util.sm4;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

public class SM4Utils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    public static final int DEFAULT_KEY_SIZE = 128;

    /**
     * SM4加密
     *
     * @param keyBytes    密钥
     * @param dataBytes   待加密明文
     * @return            加密后的字节数组
     * @throws Exception  加密异常
     */
    public static byte[] encrypt(byte[] keyBytes, byte[] dataBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        keyGenerator.init(DEFAULT_KEY_SIZE);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(dataBytes);
    }

    /**
     * SM4解密
     *
     * @param keyBytes    密钥
     * @param cipherBytes 待解密密文
     * @return            解密后的字节数组
     * @throws Exception  解密异常
     */
    public static byte[] decrypt(byte[] keyBytes, byte[] cipherBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        keyGenerator.init(DEFAULT_KEY_SIZE);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM_NAME);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(cipherBytes);
    }

    /**
     * SM4加密并返回Base64编码字符串
     *
     * @param key     密钥
     * @param data    待加密明文
     * @return        Base64编码的加密字符串
     * @throws Exception 加密异常
     */
    public static String encrypt(String key, String data) throws Exception {
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] dataBytes = data.getBytes("UTF-8");
        byte[] cipherBytes = encrypt(keyBytes, dataBytes);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    /**
     * SM4解密Base64编码的密文
     *
     * @param key           密钥
     * @param cipherText    Base64编码的密文
     * @return              解密后的明文
     * @throws Exception    解密异常
     */
    public static String decrypt(String key, String cipherText) throws Exception {
        byte[] keyBytes = key.getBytes("UTF-8");
        byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
        byte[] dataBytes = decrypt(keyBytes, cipherBytes);
        return new String(dataBytes, "UTF-8");
    }

    /**
     * 生成SM4密钥
     *
     * @return 16字节的密钥
     * @throws Exception 生成密钥异常
     */
    public static byte[] generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        keyGenerator.init(DEFAULT_KEY_SIZE);
        return keyGenerator.generateKey().getEncoded();
    }
}