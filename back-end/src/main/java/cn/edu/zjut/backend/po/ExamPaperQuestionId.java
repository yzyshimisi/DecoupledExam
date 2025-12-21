package cn.edu.zjut.backend.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperQuestionId implements java.io.Serializable {

    @Column(name = "paper_id")
    private Long paperId;

    @Column(name = "question_id")
    private Long questionId;
}
