package cn.edu.zjut.backend.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Base64Test {
    public static void main(String[] args) {
        try {
            String filePath = "C:\\Users\\31986\\Desktop\\综合学科测试题集.docx"; // 替换为你的文件路径
            String outputFilePath = "C:\\Users\\31986\\Desktop\\output_base64.txt";
            String base64String = encodeFileToBase64(filePath);
            Files.write(Paths.get(outputFilePath), base64String.getBytes());
        } catch (IOException e) {
            System.err.println("文件处理错误: " + e.getMessage());
        }
    }

    public static String encodeFileToBase64(String filePath) throws IOException {
        // 读取文件所有字节
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
        // 转换为Base64编码
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
