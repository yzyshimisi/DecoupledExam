package cn.edu.zjut.backend.po;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "student_course")
@IdClass(StudentCoursePK.class)
public class StudentCourse implements Serializable {
    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "course_id")
    private Long courseId;

    @Transient
    private Date joinTime;

    @Transient
    private String status; // 0=正常学习, 1=已退课

    // 兼容旧逻辑：保留 getId()/setId() 调用，但返回 null（表使用复合主键）
    @Transient
    public Long getId() {
        return null;
    }

    @Transient
    public void setId(Long id) {
        // no-op
    }
}