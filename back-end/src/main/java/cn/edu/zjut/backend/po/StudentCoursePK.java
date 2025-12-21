package cn.edu.zjut.backend.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class StudentCoursePK implements Serializable {
    private Long studentId;
    private Long courseId;

    public StudentCoursePK() {}

    public StudentCoursePK(Long studentId, Long courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }
}
