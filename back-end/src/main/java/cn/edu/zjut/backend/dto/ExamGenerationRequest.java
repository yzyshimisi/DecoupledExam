package cn.edu.zjut.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamGenerationRequest {

    private String paperName;

    /**
     * 学科 ID（如 1=Java, 2=Python）
     */
    private Integer subjectId;

    private Long courseId;

    private Integer totalScore = 100;

    /**
     * 总题目数量（可选，若各题型数量之和已明确，可不填）
     */
    private Integer totalQuestions;

    /**
     * 各题型的具体要求
     */
    private List<QuestionTypeRequirement> questionTypes;

    @Data
    public static class QuestionTypeRequirement {

        /**
         * 题型 ID
         */
        private Integer typeId;

        /**
         * 该题型需要的题目数量
         */
        private Integer count;

        /**
         * 难度（1-5，可选；若未指定，则使用全局难度或不限）
         */
        private Byte difficulty;

        /**
         * 知识点标签列表（如 ["HashMap", "volatile"]）
         */
        private List<String> tags;

        /**
         * 自然语言描述（如 "希望考察线程安全相关的集合类"）
         */
        private String description;
    }
}
