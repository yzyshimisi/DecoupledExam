package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "teacher_position")
public class TeacherPosition implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "role", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Byte role = 0; // 0=任课老师 1=教务老师

    // 构造函数
    public TeacherPosition() {}

    public TeacherPosition(Long id, Long teacherId, Byte role) {
        this.id = id;
        this.teacherId = teacherId;
        this.role = role;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Byte getRole() {
        return role;
    }

    public void setRole(Byte role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "TeacherPosition{" +
                "id=" + id +
                ", teacherId=" + teacherId +
                ", role=" + role +
                '}';
    }
}