package cn.edu.zjut.backend.po;

import java.io.Serializable;
import java.util.Objects;

public class TeacherSubjectId implements Serializable {
    private Long teacherId;
    private Integer subjectId;

    public TeacherSubjectId() {}

    public TeacherSubjectId(Long teacherId, Integer subjectId) {
        this.teacherId = teacherId;
        this.subjectId = subjectId;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeacherSubjectId that = (TeacherSubjectId) o;
        return Objects.equals(teacherId, that.teacherId) && Objects.equals(subjectId, that.subjectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teacherId, subjectId);
    }
}