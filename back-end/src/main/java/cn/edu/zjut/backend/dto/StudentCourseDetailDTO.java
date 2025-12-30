package cn.edu.zjut.backend.dto;

import cn.edu.zjut.backend.po.Course;
import lombok.Data;

import java.util.Date;

@Data
public class StudentCourseDetailDTO {
    private Long studentId;
    private Long courseId;
    private Date joinTime;
    private String status;
    private Course course; // 完整课程信息
}
