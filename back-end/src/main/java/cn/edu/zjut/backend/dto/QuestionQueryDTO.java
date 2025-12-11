package cn.edu.zjut.backend.dto;

public class QuestionQueryDTO {
    // 可以通过这些字段筛选题目
    private Integer typeId;          // 题型ID
    private Byte difficulty;         // 难度（1-5）
    private Integer subjectId;       // 科目ID
    private Long creatorId;          // 创建人ID
    private Byte reviewStatus;       // 审核状态

    public QuestionQueryDTO() {}

    public QuestionQueryDTO(Integer typeId, Byte difficulty, Integer subjectId, Long creatorId, Byte reviewStatus) {
        this.typeId = typeId;
        this.difficulty = difficulty;
        this.subjectId = subjectId;
        this.creatorId = creatorId;
        this.reviewStatus = reviewStatus;
    }

    @Override
    public String toString() {
        return "QuestionQueryDTO{" +
                "typeId=" + typeId +
                ", difficulty=" + difficulty +
                ", subjectId=" + subjectId +
                ", creatorId=" + creatorId +
                ", reviewStatus=" + reviewStatus +
                '}';
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Byte getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Byte difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Byte getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Byte reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}
