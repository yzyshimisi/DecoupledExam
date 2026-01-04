package cn.edu.zjut.backend.dto;

import java.util.List;

public class ExamStudentDTO {
    private Long examId;
    private List<Long> studentIds;

    // Getters and Setters
    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public List<Long> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<Long> studentIds) {
        this.studentIds = studentIds;
    }
}