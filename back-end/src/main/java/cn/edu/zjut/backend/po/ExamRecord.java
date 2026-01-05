package cn.edu.zjut.backend.po;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "exam_record")
@Data
public class ExamRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "objective_score", precision = 5, scale = 1)
    private BigDecimal objectiveScore;

    @Column(name = "subjective_score", precision = 5, scale = 1)
    private BigDecimal subjectiveScore;

    @Column(name = "total_score", precision = 5, scale = 1)
    private BigDecimal totalScore;

    @Column(name = "ai_comment", length = 65535)
    private String aiComment;

    @Column(name = "status", columnDefinition = "char(1) default '0'")
    private String status;

    @Column(name = "submit_time")
    private Date submitTime;

    // Constructors
    public ExamRecord() {}

    public ExamRecord(Long examId, Long studentId) {
        this.examId = examId;
        this.studentId = studentId;
        this.status = "0"; // 未考
    }

    // Getters and Setters
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(BigDecimal objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public BigDecimal getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(BigDecimal subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getAiComment() {
        return aiComment;
    }

    public void setAiComment(String aiComment) {
        this.aiComment = aiComment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
    
    /**
     * 获取状态描述
     * @return 状态描述字符串
     */
    public String getStatusDescription() {
        switch (status) {
            case "0": return "未考";
            case "1": return "已交";
            case "2": return "已阅";
            default: return "未知状态";
        }
    }
}