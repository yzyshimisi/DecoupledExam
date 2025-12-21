package cn.edu.zjut.backend.util;

import cn.edu.zjut.backend.dao.QuestionDAO;
import cn.edu.zjut.backend.dto.DeepSeekRequest;
import cn.edu.zjut.backend.dto.DeepSeekResponse;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Deekseek {
    private static final Log log = LogFactory.getLog(Deekseek.class);

    public static String inquire(String askQuestion) throws IOException{
        String baseUrl = "http://127.0.0.1:11434/api/chat";
        /**
         * 创建DeepSeekRequest对象 属性注解均在实体类中
         * 官方案例文档地址：https://api-docs.deepseek.com/zh-cn/api/create-chat-completion
         */
        DeepSeekRequest requestObject = new DeepSeekRequest();
        List<DeepSeekRequest.Message> messages = new ArrayList<>();
        // 添加消息 根据自身情况调整
        messages.add(new DeepSeekRequest.Message(askQuestion, "user"));
        requestObject.setMessages(messages);
        // 模型 根据自身情况调整
        requestObject.setModel("deepseek-r1:8b");
        requestObject.setFrequency_penalty(0);
        requestObject.setMax_tokens(2048);
        requestObject.setPresence_penalty(0);
        requestObject.setResponse_format(new DeepSeekRequest.ResponseFormat("text"));
        requestObject.setStop(null);
        requestObject.setStream(false);
        requestObject.setStream_options(null);
        requestObject.setTemperature(1);
        requestObject.setTop_p(1);
        requestObject.setTools(null);
        requestObject.setTool_choice("none");
        requestObject.setLogprobs(false);
        requestObject.setTop_logprobs(null);
        // 使用Gson将请求对象转换为JSON字符串
        Gson gson = new Gson();
        String jsonBody = gson.toJson(requestObject);
        // 创建OkHttpClient实例，并设置超时时间
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        // 设置请求体的媒体类型为JSON
        MediaType mediaType = MediaType.parse("application/json");
        // 创建请求体，包含JSON字符串
        RequestBody body = RequestBody.create(mediaType, jsonBody);
        // 创建HTTP POST请求
        Request request = new Request.Builder()
                .url(baseUrl)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        System.out.println("正在生成...");
        // 发送请求并获取响应
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            try {
                assert response.body() != null;
                String responseBody = response.body().string();
                System.out.println("responseBody: " + responseBody);
                //将响应体内容转换为DeepSeekResponse对象
                DeepSeekResponse deepSeekResponse = gson.fromJson(responseBody, DeepSeekResponse.class);
                return deepSeekResponse.getMessage().getContent();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }else{
            return "";
        }
    }
}
