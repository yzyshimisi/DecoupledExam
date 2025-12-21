package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.util.sm4.SM4Utils;
import java.util.Base64;

/**
 * 密码编码器工具类
 * 使用SM4算法对用户密码进行加密和验证
 */
public class PasswordEncoder {
    // 固定的密钥，实际项目中应该从配置文件读取或者更安全地存储
    private static final String SECRET_KEY = "DecoupledExamKey";

    /**
     * 对密码进行加密
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encode(CharSequence rawPassword) {
        try {
            return SM4Utils.encrypt(SECRET_KEY, rawPassword.toString());
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            String decodedPassword = SM4Utils.decrypt(SECRET_KEY, encodedPassword);
            return rawPassword.toString().equals(decodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}