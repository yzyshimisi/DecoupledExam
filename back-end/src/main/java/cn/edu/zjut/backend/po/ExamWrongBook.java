package cn.edu.zjut.backend.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "exam_wrong_book")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamWrongBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 1;

    @Column(name = "create_time", nullable = false, updatable = false)
    private Date createTime;

    @Column(name = "last_error_time")
    private Date lastErrorTime;

    @Column(name = "is_deleted", nullable = false, length = 1)
    private String isDeleted = "0";

    // Getters and Setters
}

