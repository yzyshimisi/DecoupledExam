package cn.edu.zjut.backend.po;

import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "exam_paper")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    private Long paperId;

    @Column(name = "paper_name", nullable = false, length = 100)
    private String paperName;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 100;

    @Column(name = "compose_type", nullable = false, length = 1)
    private String composeType = "1";

    @Column(name = "is_sealed", nullable = false, length = 1)
    private String isSealed = "0";

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime =  new Date();

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt =  new Date();
}

