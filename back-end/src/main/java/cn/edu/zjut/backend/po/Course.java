package cn.edu.zjut.backend.po;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "edu_course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;
    
    @Column(name = "course_name")
    private String courseName;
    
    @Column(name = "invite_code")
    private String inviteCode;
    
    @Column(name = "subject_id")
    private Integer subjectId;
    
    @Column(name = "teacher_id")
    private Long teacherId;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "status")
    private String status; // 0=开设, 1=结课
    
    @Column(name = "create_time")
    private Date createTime;
}