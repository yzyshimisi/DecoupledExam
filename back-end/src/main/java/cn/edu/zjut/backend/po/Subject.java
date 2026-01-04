package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.util.Date;

@Entity // 1. 标记这是一个 JPA 实体类，对应 XML 中的 <class>
@Table(name = "subject") // 2. 对应 XML 中的 <class> 标签的 table 和 catalog 属性
public class Subject {
    // 学科ID
    @Id // 3. 标记主键，对应 XML 中的 <id>
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. 对应 XML 中的 <generator class="identity"/>
    @Column(name = "subject_id") // 5. 对应 XML 中的 <id> 标签的 column 属性
    private Integer subjectId;

    @Column(name = "subject_name", length = 50, nullable = false) // 6. 对应 XML 中的 <property> 标签
    private String subjectName;

    @Column(name = "subject_code", length = 20, nullable = false)
    private String subjectCode;

    @Column(name = "grade_level", nullable = false)
    private Byte gradeLevel;

    @Column(name = "sort_order", nullable = false, columnDefinition = "INT(11) DEFAULT 0") // 7. 使用 columnDefinition 来实现默认值
    private Integer sortOrder = 1;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Byte status;

    @Column(name = "create_time", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date createTime = new Date();

    public Subject() {}

    public Subject(Integer subjectId, String subjectName, String subjectCode, Byte gradeLevel, Integer sortOrder, Byte status, Date createTime) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
        this.gradeLevel = gradeLevel;
        this.sortOrder = sortOrder;
        this.status = status;
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "subjectId=" + subjectId +
                ", subjectName='" + subjectName + '\'' +
                ", subjectCode='" + subjectCode + '\'' +
                ", gradeLevel=" + gradeLevel +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public Byte getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(Byte gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
