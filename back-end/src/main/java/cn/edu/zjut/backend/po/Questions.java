package cn.edu.zjut.backend.po;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_type", columnList = "type_id"),
        @Index(name = "idx_subject", columnList = "subject_id"),
        @Index(name = "idx_creator", columnList = "creator_id"),
        @Index(name = "idx_review", columnList = "review_status")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"  // 使用实体的 id 字段作为唯一标识
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 适配MySQL AUTO_INCREMENT
    private Long id;

    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "difficulty", nullable = false)
    private Byte difficulty = 1; // 难度 1-5

    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId = -1L;

    @Column(name = "review_status", nullable = false)
    private Byte reviewStatus = 0; // 默认值0

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date();

    // ==================== 关联关系 ====================
    /** 关联子题（一对多）：一个题目包含多个子题 */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, // 级联增删改
            orphanRemoval = true,   // 移除子题时自动删除数据库记录
            fetch = FetchType.EAGER)
//    @JoinColumn(name = "question_id")
//    @JsonManagedReference
    private Set<QuestionItems> questionItems;

    /** 关联题目组件（一对多）：题目直接关联的组件 */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "question_id")
//    @JsonManagedReference
    private Set<QuestionComponents> questionComponents;
}
