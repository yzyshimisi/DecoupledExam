package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "exam_notification")
public class ExamNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "student_id")
    private Long studentId; // 如果为null，则表示发送给所有参加该考试的学生

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "send_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendTime;

    @Column(name = "is_sent", nullable = false)
    private Boolean isSent = false; // 是否已发送

    @Column(name = "send_attempts")
    private Integer sendAttempts = 0; // 发送尝试次数

    public ExamNotification() {}

    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Boolean getIsSent() {
        return isSent;
    }

    public void setIsSent(Boolean isSent) {
        this.isSent = isSent;
    }

    public Integer getSendAttempts() {
        return sendAttempts;
    }

    public void setSendAttempts(Integer sendAttempts) {
        this.sendAttempts = sendAttempts;
    }
}