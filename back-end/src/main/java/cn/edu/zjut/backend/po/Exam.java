package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "exam")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "paper_id", nullable = false)
    private Long paperId;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(name = "exam_code", length = 20)
    private String examCode;

    @Column(name = "status", nullable = false)
    private Byte status; // 匹配SQL的TINYINT(1)

    // 【删除】SQL表中无batch/session字段
    // private String batch;
    // private Integer session;

    // 【删除】取消级联关联（改为单独保存ExamSetting）
    // private ExamSetting examSetting;

    public Exam() {}

    // Getters and Setters（仅保留SQL表中存在的字段）
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }
    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
}