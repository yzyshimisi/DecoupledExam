package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "student_grade", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_course_id", columnList = "course_id"),
        @Index(name = "idx_subject_id", columnList = "subject_id"),
        @Index(name = "idx_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_grade_type", columnList = "grade_type"),
        @Index(name = "idx_record_time", columnList = "record_time")
})
public class StudentGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long gradeId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "grade_type", nullable = false, length = 30)
    private String gradeType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "grade_name", nullable = false, length = 100)
    private String gradeName;

    @Column(name = "score", nullable = false, precision = 5, scale = 1)
    private BigDecimal score;

    @Column(name = "full_score", nullable = false, precision = 5, scale = 1)
    private BigDecimal fullScore;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "record_time", nullable = false)
    private Date recordTime;

    @Column(name = "remark", length = 255)
    private String remark;

    // Constructors
    public StudentGrade() {}

    // Getters and Setters
    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getFullScore() {
        return fullScore;
    }

    public void setFullScore(BigDecimal fullScore) {
        this.fullScore = fullScore;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "StudentGrade{" +
                "gradeId=" + gradeId +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", subjectId=" + subjectId +
                ", gradeType='" + gradeType + '\'' +
                ", sourceId=" + sourceId +
                ", gradeName='" + gradeName + '\'' +
                ", score=" + score +
                ", fullScore=" + fullScore +
                ", teacherId=" + teacherId +
                ", recordTime=" + recordTime +
                ", remark='" + remark + '\'' +
                '}';
    }
}