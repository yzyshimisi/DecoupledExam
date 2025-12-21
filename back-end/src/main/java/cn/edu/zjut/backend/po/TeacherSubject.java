package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "teacher_subject")
@IdClass(TeacherSubjectId.class)
public class TeacherSubject implements Serializable {
    @Id
    @Column(name = "teacher_id")
    private Long teacherId;

    @Id
    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "is_main", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Byte isMain = 0; // 0=兼任 1=主讲

    // 构造函数
    public TeacherSubject() {}

    public TeacherSubject(Long teacherId, Integer subjectId, Byte isMain) {
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.isMain = isMain;
    }

    // Getter和Setter方法
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Byte getIsMain() {
        return isMain;
    }

    public void setIsMain(Byte isMain) {
        this.isMain = isMain;
    }

    @Override
    public String toString() {
        return "TeacherSubject{" +
                "teacherId=" + teacherId +
                ", subjectId=" + subjectId +
                ", isMain=" + isMain +
                '}';
    }
}