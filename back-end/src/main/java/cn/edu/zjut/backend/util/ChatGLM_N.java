package cn.edu.zjut.backend.util;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.fileparsing.FileParsingDownloadReq;
import ai.z.openapi.service.fileparsing.FileParsingDownloadResponse;
import ai.z.openapi.service.fileparsing.FileParsingResponse;
import ai.z.openapi.service.fileparsing.FileParsingUploadReq;
import ai.z.openapi.service.model.*;
import java.util.Collections;

public class ChatGLM_N {

    private static final String API_KEY = "dc2dac203e4847a38277beb1f2ce5936.TUemDuFlJBO7fX2F";;

    public static String inquire(String askQuestion, Boolean useKnowledge) {
        // 初始化客户端
        ZhipuAiClient client = ZhipuAiClient.builder()
                .apiKey(API_KEY)
                .build();

        ChatCompletionCreateParams request = null;
        if(useKnowledge){
             request = ChatCompletionCreateParams.builder()
                    .model("glm-4.6")
                    .messages(Collections.singletonList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(askQuestion)
                                    .build()
                    ))
                    .tools(Collections.singletonList(ChatTool.builder()
                            .type("retrieval")
                            .retrieval(Retrieval.builder()
                                    .knowledgeId("2002335150846976000")
                                    .promptTemplate("从知识库\\n\\\"\\\"\\\"\\n{{knowledge}}\\n\\\"\\\"\\\"\\n，提取与问题相关的所有标签（只要召回率高就可以包含进来），生成问题\\n\\\"\\\"\\\"\\n{{question}}\\n\\\"\\\"\\\"\\n的答案，不要复述问题，直接开始回答。")
                                    .build()).build()
                    ))
                    .thinking(ChatThinking.builder().type("disabled").build())
                    .build();
        }else {
             request = ChatCompletionCreateParams.builder()
                    .model("glm-4.6")
                    .messages(Collections.singletonList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(askQuestion)
                                    .build()
                    ))
                    .thinking(ChatThinking.builder().type("disabled").build())
                    .build();
        }
        // 发送请求
        ChatCompletionResponse response = client.chat().createChatCompletion(request);

        // 获取回复
        if (response.isSuccess()) {
            Object reply = response.getData().getChoices().get(0).getMessage().getContent();
            return reply.toString();
        } else {
            return "错误: " + response + "\n" + response.getMsg();
        }
    }
}
