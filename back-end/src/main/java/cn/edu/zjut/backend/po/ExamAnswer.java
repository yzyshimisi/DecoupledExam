package cn.edu.zjut.backend.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exam_answer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "my_answer", columnDefinition = "TEXT")
    private String myAnswer;

    @Column(name = "score", precision = 5, scale = 1)
    private BigDecimal score;

    @Column(name = "marker_id")
    private Long markerId;

    @Column(name = "mark_type", nullable = false, length = 1)
    private String markType; // 或者使用 Character 类型

    // Getters and Setters
}

