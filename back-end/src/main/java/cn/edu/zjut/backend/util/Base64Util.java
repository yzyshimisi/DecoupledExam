package cn.edu.zjut.backend.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class Base64Util {

    public static InputStream base64ToInputStream(String base64String) {
        // 1. 去除 Data URL 前缀（如果存在）
        if (base64String.contains(",")) {
            base64String = base64String.split(",", 2)[1];
        }

        // 2. Base64 解码为字节数组
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        // 3. 包装为 InputStream
        return new ByteArrayInputStream(decodedBytes);
    }
}
