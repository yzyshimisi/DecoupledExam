package cn.edu.zjut.backend.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "exam_course")
public class ExamCourse {
    @EmbeddedId
    private ExamCoursePK id;

    @Column(name = "publish_time")
    private java.util.Date publishTime;

    @Column(name = "publisher_id")
    private Long publisherId;

    public ExamCourse() {}

}