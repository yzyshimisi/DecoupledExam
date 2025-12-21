package cn.edu.zjut.backend.po;

import javax.persistence.*;
import java.math.BigDecimal;

// 核心注解1：标记为Hibernate实体
@Entity
// 核心注解2：映射到数据库表（表名需与实际一致）
@Table(name = "exam_setting")
public class ExamSetting {
    // 核心注解3：主键（必须有，Hibernate要求所有实体有主键）
    @Id
    @Column(name = "setting_id")
    // 主键自增（匹配MySQL AUTO_INCREMENT）
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId; // 数据库表的主键字段（如id）

    @Column(name = "exam_id")
    private Long examId;

    // 其他字段映射（与数据库字段一一对应）
    @Column(name = "duration_minute")
    private Integer durationMinute;

    @Column(name = "allow_late_enter")
    private Byte allowLateEnter;

    @Column(name = "question_shuffle")
    private Byte questionShuffle;

    @Column(name = "option_shuffle")
    private Byte optionShuffle;

    @Column(name = "prevent_screen_switch")
    private Byte preventScreenSwitch;

    @Column(name = "passing_score")
    private BigDecimal passingScore;

    @Column(name = "auto_submit")
    private Byte autoSubmit;

    @Column(name = "allow_view_paper")
    private Byte allowViewPaper;

    @Column(name = "allow_view_score")
    private Byte allowViewScore;

    @Column(name = "generate_exam_code")
    private Byte generateExamCode;

    @Column(name = "peer_review")
    private Byte peerReview;

    @Column(name = "fill_case_sensitive")
    private Byte fillCaseSensitive;

    @Column(name = "fill_ignore_symbols")
    private Byte fillIgnoreSymbols;

    @Column(name = "fill_manual_mark")
    private Byte fillManualMark;

    @Column(name = "multi_choice_partial_score")
    private Byte multiChoicePartialScore;

    @Column(name = "multi_choice_partial_ratio")
    private BigDecimal multiChoicePartialRatio;

    @Column(name = "sort_question_score_per_blank")
    private Byte sortQuestionScorePerBlank;

    // 必须有默认构造函数（Hibernate反射创建实例需要）
    public ExamSetting() {}

    // 完整的Getters & Setters（缺一不可）
    public Long getSettingId() { return settingId; }
    public void setSettingId(Long settingId) { this.settingId = settingId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
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