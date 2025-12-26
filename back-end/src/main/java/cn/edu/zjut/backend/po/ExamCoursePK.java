package cn.edu.zjut.backend.po;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Setter
@Getter
@Embeddable
public class ExamCoursePK implements Serializable {
    @Column(name = "exam_id")
    private Long examId;

    @Column(name = "course_id")
    private Long courseId;

    public ExamCoursePK() {}

    public ExamCoursePK(Long examId, Long courseId) {
        this.examId = examId;
        this.courseId = courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamCoursePK that = (ExamCoursePK) o;

        if (examId != null ? !examId.equals(that.examId) : that.examId != null) return false;
        return courseId != null ? !courseId.equals(that.courseId) : that.courseId != null;
    }

    @Override
    public int hashCode() {
        int result = examId != null ? examId.hashCode() : 0;
        result = 31 * result + (courseId != null ? courseId.hashCode() : 0);
        return result;
    }
}