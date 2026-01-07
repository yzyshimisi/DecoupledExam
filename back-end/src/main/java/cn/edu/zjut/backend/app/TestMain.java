package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.service.ExamPaperService;
import cn.edu.zjut.backend.util.ChatGLM_N;
import org.hibernate.boot.cfgxml.internal.ConfigLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class TestMain {

    private static final Properties properties = new Properties();

    public static void main(String[] args) throws IOException {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("无法找到 config.properties 文件");
            }
            properties.load(input);

            System.out.println(properties.getProperty("app.resource.path"));
        } catch (IOException e) {
            throw new RuntimeException("加载 config.properties 失败", e);
        }
    }
}
