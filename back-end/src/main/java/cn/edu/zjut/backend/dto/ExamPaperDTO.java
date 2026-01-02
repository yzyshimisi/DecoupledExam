package cn.edu.zjut.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ExamPaperDTO {

    private Long paperId;
    private String paperName;
    private Long courseId;
    private Integer totalScore;
    private String isSealed;
    private Long creatorId;
    private String composeType = "1";
    private Date createTime =  new Date();
    private Date updatedAt =  new Date();

    private List<QuestionInfoDTO> questions = new ArrayList<>();

    @Data
    public static class QuestionInfoDTO {
        private Long questionId;
        private BigDecimal score;
        private Integer sortOrder;
    }
}
