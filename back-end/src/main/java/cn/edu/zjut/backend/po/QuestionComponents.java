package cn.edu.zjut.backend.po;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

@Entity
@Table(name = "question_components",
        indexes = {
                @Index(name = "idx_question", columnList = "question_id"),
                @Index(name = "idx_item", columnList = "item_id"),
                @Index(name = "idx_type", columnList = "component_type")
        })
public class QuestionComponents {
    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    /** 关联主题目ID（可空） */
//    @Column(name = "question_id")
//    private Long questionId;

//    /** 关联子题ID（可空） */
//    @Column(name = "item_id")
//    private Long itemId;

    /** 组件类型：stem/option/answer/analysis/audio/code等 */
    @Column(name = "component_type", nullable = false, length = 20)
    private String componentType;

    /** 组件内容（JSON格式） */
    @Column(name = "content", columnDefinition = "JSON")
    private String content; // 用String存储JSON，Hibernate 6.x可改用JsonType

    /** 扩展信息（JSON格式） */
    @Column(name = "meta", columnDefinition = "JSON")
    private String meta;

    // ==================== 关联关系 ====================
    /** 关联主题目（多对一，可空） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id",
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
//    @JsonBackReference
    private Questions question;

    /** 关联子题（多对一，可空） */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id",
            foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
//    @JsonBackReference
    private QuestionItems item;

    public QuestionComponents() {}

    public QuestionComponents(Long id, String componentType, String content, String meta, Questions question, QuestionItems item) {
        this.id = id;
//        this.questionId = questionId;
//        this.itemId = itemId;
        this.componentType = componentType;
        this.content = content;
        this.meta = meta;
        this.question = question;
        this.item = item;
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
//
//    public Long getItemId() {
//        return itemId;
//    }
//
//    public void setItemId(Long itemId) {
//        this.itemId = itemId;
//    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Questions getQuestion() {
        return question;
    }

    public void setQuestion(Questions question) {
        this.question = question;
    }

    public QuestionItems getItem() {
        return item;
    }

    public void setItem(QuestionItems item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "QuestionComponents{" +
                "id=" + id +
                ", componentType='" + componentType + '\'' +
                ", content='" + content + '\'' +
                ", meta='" + meta + '\'' +
                '}';
    }
}
