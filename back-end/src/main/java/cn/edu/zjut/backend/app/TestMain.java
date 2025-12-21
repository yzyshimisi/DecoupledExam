package cn.edu.zjut.backend.app;

import cn.edu.zjut.backend.service.ExamPaperService;
import cn.edu.zjut.backend.util.ChatGLM_N;

import java.io.IOException;



public class TestMain {
    public static void main(String[] args) throws IOException {
        ExamPaperService service = new ExamPaperService();
        service.exportQuestionsWithTagsToCsv("C:\\Users\\31986\\Desktop\\questionWithTags.csv");
//        String prompt = "你是一个专业的智能组卷系统。请根据以下要求，从题库中选择合适的题目 ID。\n" +
//                "\n" +
//                "【组卷要求】\n" +
//                "学科：Java 编程\n" +
//                "总题数：8\n" +
//                "难度：中级\n" +
//                "题型与知识点分布：\n" +
//                "单选题（3 道）：需覆盖知识点 [\"集合\", \"HashMap\"]\n" +
//                "多选题（2 道）：需覆盖知识点 [\"并发\", \"线程池\"]\n" +
//                "简答题（3 道）：需覆盖知识点 [\"JVM\", \"垃圾回收\"]\n" +
//                "\n" +
//                "【任务】\n" +
//                "1. 从知识库中为每个题型选择指定数量的题目；\n" +
//                "2. 优先选择标签与要求知识点语义相关或完全匹配的题目（如果实在没有，可以选择其他相近的题目，一定要确保数量符合要求）；\n" +
//                "3. 尽量覆盖所有指定的知识点；\n" +
//                "4. 不要重复选题\n" +
//                "\n" +
//                "【输出格式】\n" +
//                "仅返回选中的题目 ID，每行一个，不要解释，不要包含其他内容。\n" +
//                "例如：\n" +
//                "1\n" +
//                "5\n" +
//                "12\n" +
//                "23";
////        System.out.println(prompt);
//
//        String res = ChatGLM_N.inquire(prompt, false);
//        System.out.println("===============================");
//        System.out.println(res);
    }
}
