package cn.edu.zjut.backend.po;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "question_items",
        indexes = {
                @Index(name = "idx_question", columnList = "question_id"),
                @Index(name = "idx_type", columnList = "type_id")
        })
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"  // 使用实体的 id 字段作为唯一标识
)
public class QuestionItems {
    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联主题目ID（外键） */
//    @Column(name = "question_id", nullable = false)
//    private Long questionId;

    /** 子题顺序 */
    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    /** 子题题干 */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** 子题类型 */
    @Column(name = "type_id", nullable = false)
    private Integer typeId;

    // ==================== 关联关系 ====================
    /** 关联主题目（多对一） */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id",
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
//    @JsonBackReference
    private Questions question;

    /** 关联子题组件（一对多）：子题关联的组件 */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//    @JoinColumn(name = "item_id")
//    @JsonManagedReference
    private List<QuestionComponents> questionComponents;

    public QuestionItems() {}

    public QuestionItems(Long id, Integer sequence, String content, Integer typeId, Questions question, List<QuestionComponents> questionComponents) {
        this.id = id;
//        this.questionId = questionId;
        this.sequence = sequence;
        this.content = content;
        this.typeId = typeId;
        this.question = question;
        this.questionComponents = questionComponents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public Long getQuestionId() {
//        return questionId;
//    }
//
//    public void setQuestionId(Long questionId) {
//        this.questionId = questionId;
//    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Questions getQuestion() {
        return question;
    }

    public void setQuestion(Questions question) {
        this.question = question;
    }

    public List<QuestionComponents> getQuestionComponents() {
        return questionComponents;
    }

    public void setQuestionComponents(List<QuestionComponents> questionComponents) {
        this.questionComponents = questionComponents;
    }

    @Override
    public String toString() {
        return "QuestionItems{" +
                "id=" + id +
                ", sequence=" + sequence +
                ", content='" + content + '\'' +
                ", typeId=" + typeId +
                ", questionComponents=" + questionComponents +
                '}';
    }
}
