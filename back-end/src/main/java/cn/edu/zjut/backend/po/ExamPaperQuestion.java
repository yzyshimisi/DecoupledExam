package cn.edu.zjut.backend.po;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exam_paper_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaperQuestion {

    @EmbeddedId
    private ExamPaperQuestionId id;

    @Column(name = "score", precision = 5, scale = 1, nullable = false)
    private BigDecimal score;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
