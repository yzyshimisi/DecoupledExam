package cn.edu.zjut.backend.po;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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
            orphanRemoval = true) // 移除子题时自动删除数据库记录
//    @JoinColumn(name = "question_id")
//    @JsonManagedReference
    private List<QuestionItems> questionItems;

    /** 关联题目组件（一对多）：题目直接关联的组件 */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "question_id")
//    @JsonManagedReference
    private List<QuestionComponents> questionComponents;

    public Questions() {}

    public Questions(Long id, Integer typeId, String title, Byte difficulty, Integer subjectId, Long creatorId, Byte reviewStatus, Date createdAt, Date updatedAt, List<QuestionItems> questionItems, List<QuestionComponents> questionComponents) {
        this.id = id;
        this.typeId = typeId;
        this.title = title;
        this.difficulty = difficulty;
        this.subjectId = subjectId;
        this.creatorId = creatorId;
        this.reviewStatus = reviewStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.questionItems = questionItems;
        this.questionComponents = questionComponents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Byte getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Byte difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Byte getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Byte reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QuestionItems> getQuestionItems() {
        return questionItems;
    }

    public void setQuestionItems(List<QuestionItems> questionItems) {
        this.questionItems = questionItems;
    }

    public List<QuestionComponents> getQuestionComponents() {
        return questionComponents;
    }

    public void setQuestionComponents(List<QuestionComponents> questionComponents) {
        this.questionComponents = questionComponents;
    }

    @Override
    public String toString() {
        return "Questions{" +
                "id=" + id +
                ", typeId=" + typeId +
                ", title='" + title + '\'' +
                ", difficulty=" + difficulty +
                ", subjectId=" + subjectId +
                ", creatorId=" + creatorId +
                ", reviewStatus=" + reviewStatus +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", questionItems=" + questionItems +
                ", questionComponents=" + questionComponents +
                '}';
    }
}
