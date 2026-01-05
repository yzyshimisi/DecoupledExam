package cn.edu.zjut.backend.dto;

import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamSetting;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 考试详情DTO，包含考试基本信息和考试设置信息
 */
public class ExamDetailDTO {
    // 考试基本信息字段
    private Long examId;
    private String title;
    private Long paperId;
    private Long teacherId;
    private Date startTime;
    private Date endTime;
    private String examCode;
    private Byte status;

    // 考试设置字段
    private Integer durationMinute;
    private Byte allowLateEnter;
    private Byte questionShuffle;
    private Byte optionShuffle;
    private Byte preventScreenSwitch;
    private BigDecimal passingScore;
    private Byte autoSubmit;
    private Byte allowViewPaper;
    private Byte allowViewScore;
    private Byte generateExamCode;
    private Byte peerReview;
    private Byte fillCaseSensitive;
    private Byte fillIgnoreSymbols;
    private Byte fillManualMark;
    private Byte multiChoicePartialScore;
    private BigDecimal multiChoicePartialRatio;
    private Byte sortQuestionScorePerBlank;

    public ExamDetailDTO() {}

    public ExamDetailDTO(Exam exam, ExamSetting examSetting) {
        // 设置考试基本信息
        if (exam != null) {
            this.examId = exam.getExamId();
            this.title = exam.getTitle();
            this.paperId = exam.getPaperId();
            this.teacherId = exam.getTeacherId();
            this.startTime = exam.getStartTime();
            this.endTime = exam.getEndTime();
            this.examCode = exam.getExamCode();
            this.status = exam.getStatus();
        }

        // 设置考试设置信息
        if (examSetting != null) {
            this.durationMinute = examSetting.getDurationMinute();
            this.allowLateEnter = examSetting.getAllowLateEnter();
            this.questionShuffle = examSetting.getQuestionShuffle();
            this.optionShuffle = examSetting.getOptionShuffle();
            this.preventScreenSwitch = examSetting.getPreventScreenSwitch();
            this.passingScore = examSetting.getPassingScore();
            this.autoSubmit = examSetting.getAutoSubmit();
            this.allowViewPaper = examSetting.getAllowViewPaper();
            this.allowViewScore = examSetting.getAllowViewScore();
            this.generateExamCode = examSetting.getGenerateExamCode();
            this.peerReview = examSetting.getPeerReview();
            this.fillCaseSensitive = examSetting.getFillCaseSensitive();
            this.fillIgnoreSymbols = examSetting.getFillIgnoreSymbols();
            this.fillManualMark = examSetting.getFillManualMark();
            this.multiChoicePartialScore = examSetting.getMultiChoicePartialScore();
            this.multiChoicePartialRatio = examSetting.getMultiChoicePartialRatio();
            this.sortQuestionScorePerBlank = examSetting.getSortQuestionScorePerBlank();
        }
    }

    // 考试基本信息 Getters & Setters
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }
    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }

    // 考试设置 Getters & Setters
    public Integer getDurationMinute() { return durationMinute; }
    public void setDurationMinute(Integer durationMinute) { this.durationMinute = durationMinute; }
    public Byte getAllowLateEnter() { return allowLateEnter; }
    public void setAllowLateEnter(Byte allowLateEnter) { this.allowLateEnter = allowLateEnter; }
    public Byte getQuestionShuffle() { return questionShuffle; }
    public void setQuestionShuffle(Byte questionShuffle) { this.questionShuffle = questionShuffle; }
    public Byte getOptionShuffle() { return optionShuffle; }
    public void setOptionShuffle(Byte optionShuffle) { this.optionShuffle = optionShuffle; }
    public Byte getPreventScreenSwitch() { return preventScreenSwitch; }
    public void setPreventScreenSwitch(Byte preventScreenSwitch) { this.preventScreenSwitch = preventScreenSwitch; }
    public BigDecimal getPassingScore() { return passingScore; }
    public void setPassingScore(BigDecimal passingScore) { this.passingScore = passingScore; }
    public Byte getAutoSubmit() { return autoSubmit; }
    public void setAutoSubmit(Byte autoSubmit) { this.autoSubmit = autoSubmit; }
    public Byte getAllowViewPaper() { return allowViewPaper; }
    public void setAllowViewPaper(Byte allowViewPaper) { this.allowViewPaper = allowViewPaper; }
    public Byte getAllowViewScore() { return allowViewScore; }
    public void setAllowViewScore(Byte allowViewScore) { this.allowViewScore = allowViewScore; }
    public Byte getGenerateExamCode() { return generateExamCode; }
    public void setGenerateExamCode(Byte generateExamCode) { this.generateExamCode = generateExamCode; }
    public Byte getPeerReview() { return peerReview; }
    public void setPeerReview(Byte peerReview) { this.peerReview = peerReview; }
    public Byte getFillCaseSensitive() { return fillCaseSensitive; }
    public void setFillCaseSensitive(Byte fillCaseSensitive) { this.fillCaseSensitive = fillCaseSensitive; }
    public Byte getFillIgnoreSymbols() { return fillIgnoreSymbols; }
    public void setFillIgnoreSymbols(Byte fillIgnoreSymbols) { this.fillIgnoreSymbols = fillIgnoreSymbols; }
    public Byte getFillManualMark() { return fillManualMark; }
    public void setFillManualMark(Byte fillManualMark) { this.fillManualMark = fillManualMark; }
    public Byte getMultiChoicePartialScore() { return multiChoicePartialScore; }
    public void setMultiChoicePartialScore(Byte multiChoicePartialScore) { this.multiChoicePartialScore = multiChoicePartialScore; }
    public BigDecimal getMultiChoicePartialRatio() { return multiChoicePartialRatio; }
    public void setMultiChoicePartialRatio(BigDecimal multiChoicePartialRatio) { this.multiChoicePartialRatio = multiChoicePartialRatio; }
    public Byte getSortQuestionScorePerBlank() { return sortQuestionScorePerBlank; }
    public void setSortQuestionScorePerBlank(Byte sortQuestionScorePerBlank) { this.sortQuestionScorePerBlank = sortQuestionScorePerBlank; }
}