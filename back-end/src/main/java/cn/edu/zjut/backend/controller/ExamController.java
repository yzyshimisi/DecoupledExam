package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamSetting;
import cn.edu.zjut.backend.po.ExamRecord;
import cn.edu.zjut.backend.po.StudentVO;
import cn.edu.zjut.backend.service.ExamService;
import cn.edu.zjut.backend.service.ExamSettingService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import cn.edu.zjut.backend.dto.ExamStudentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    @Qualifier("examServ")
    private ExamService examService;

    @Autowired
    @Qualifier("examSettingServ")
    private ExamSettingService examSettingService;

    // 时间格式化常量（避免重复创建）
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 发布考试（修复空值/格式/参数校验）
     */
    @PostMapping
    public Response<Long> createExam(@RequestBody ExamCreateReq req) {
        try {
            // ========== 1. 前置参数校验（优先拦截错误） ==========
            // 基础非空校验
            if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
                return Response.error("考试标题不能为空");
            }
            if (req.getPaperId() == null || req.getPaperId() <= 0) {
                return Response.error("试卷ID必须大于0");
            }
            if (req.getTeacherId() == null || req.getTeacherId() <= 0) {
                return Response.error("教师ID必须大于0");
            }
            // 时间格式校验（允许为空，但不为空时必须符合格式）
            Date startTime = null;
            if (req.getStartTime() != null && !req.getStartTime().trim().isEmpty()) {
                try {
                    startTime = SDF.parse(req.getStartTime().trim());
                } catch (ParseException e) {
                    return Response.error("开始时间格式错误：请使用yyyy-MM-dd HH:mm:ss（例：2023-12-30 10:00:00）");
                }
            } else {
                return Response.error("开始时间不能为空");
            }
            Date endTime = null;
            if (req.getEndTime() != null && !req.getEndTime().trim().isEmpty()) {
                try {
                    endTime = SDF.parse(req.getEndTime().trim());
                } catch (ParseException e) {
                    return Response.error("结束时间格式错误：请使用yyyy-MM-dd HH:mm:ss（例：2023-12-30 12:00:00）");
                }
            } else {
                return Response.error("结束时间不能为空");
            }
            // 时间逻辑校验
            if (startTime.after(endTime)) {
                return Response.error("开始时间不能晚于结束时间");
            }
            // 考试时长校验
            if (req.getDurationMinute() == null || req.getDurationMinute() <= 0) {
                return Response.error("考试时长必须大于0分钟");
            }
            // 及格分数校验（不为空时）
            if (req.getPassingScore() != null) {
                if (req.getPassingScore().compareTo(BigDecimal.ZERO) < 0 || req.getPassingScore().compareTo(new BigDecimal(100)) > 0) {
                    return Response.error("及格分数必须在0-100之间");
                }
            }

            // ========== 2. 构建Exam实体（仅保留SQL字段） ==========
            Exam exam = new Exam();
            exam.setTitle(req.getTitle().trim());
            exam.setPaperId(req.getPaperId());
            exam.setTeacherId(req.getTeacherId());
            exam.setStartTime(startTime);
            exam.setEndTime(endTime);
            exam.setExamCode(req.getExamCode() != null ? req.getExamCode().trim() : null);
            exam.setStatus((byte) 0); // 初始状态：未开始

            // ========== 3. 构建ExamSetting实体（匹配SQL字段） ==========
            ExamSetting setting = new ExamSetting();
            setting.setDurationMinute(req.getDurationMinute());
            // 布尔值转Byte（补充默认值，避免null）
            setting.setAllowLateEnter((byte) (req.getAllowLateEnter() != null && req.getAllowLateEnter() ? 1 : 0));
            setting.setQuestionShuffle((byte) (req.getQuestionShuffle() != null && req.getQuestionShuffle() ? 1 : 0));
            setting.setOptionShuffle((byte) (req.getOptionShuffle() != null && req.getOptionShuffle() ? 1 : 0));
            setting.setPreventScreenSwitch((byte) (req.getPreventScreenSwitch() != null && req.getPreventScreenSwitch() ? 1 : 0));
            setting.setPassingScore(req.getPassingScore());
            setting.setAutoSubmit((byte) (req.getAutoSubmit() != null && req.getAutoSubmit() ? 1 : 0));
            setting.setAllowViewPaper((byte) (req.getAllowViewPaper() != null && req.getAllowViewPaper() ? 1 : 0));
            setting.setAllowViewScore((byte) (req.getAllowViewScore() != null && req.getAllowViewScore() ? 1 : 0));
            // 防作弊设置字段
            setting.setPeerReview((byte) (req.getPeerReview() != null && req.getPeerReview() ? 1 : 0));
            setting.setFillCaseSensitive((byte) (req.getFillCaseSensitive() != null && req.getFillCaseSensitive() ? 1 : 0));
            setting.setFillIgnoreSymbols((byte) (req.getFillIgnoreSymbols() != null && req.getFillIgnoreSymbols() ? 1 : 0));
            setting.setFillManualMark((byte) (req.getFillManualMark() != null && req.getFillManualMark() ? 1 : 0));
            // 补充SQL默认字段（避免null）
            setting.setGenerateExamCode((byte) 0);
            setting.setMultiChoicePartialScore((byte) 1);
            setting.setMultiChoicePartialRatio(req.getMultiChoicePartialRatio() != null ? req.getMultiChoicePartialRatio() : new BigDecimal(0.5));
            setting.setSortQuestionScorePerBlank((byte) 1);

            // ========== 4. 创建考试（同时保存exam和examSetting） ==========
            Long examId = examService.createExam(exam, setting);
            return examId != null ? Response.success(examId) : Response.error("创建考试失败：数据库操作异常");

        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("服务器异常：" + e.getMessage());
        }
    }

    /**
     * 查询考试列表
     * @param status 考试状态 (可选)
     * @param teacherId 教师ID (可选)
     * @return 考试列表
     */
    @GetMapping
    public Response<List<Exam>> getExamList(
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Long teacherId) {
        try {
            List<Exam> exams = examService.getExamList(status, teacherId);
            return Response.success(exams);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询考试列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取考试详情
     * @param examId 考试ID
     * @return 考试详情
     */
    @GetMapping("/{examId}")
    public Response<Exam> getExamById(@PathVariable("examId") Long examId) {
        try {
            Exam exam = examService.getExamById(examId);
            if (exam != null) {
                return Response.success(exam);
            } else {
                return Response.error("未找到指定的考试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询考试详情失败：" + e.getMessage());
        }
    }

    /**
     * 根据考试记录ID获取考试记录
     * @param recordId 考试记录ID
     * @return 考试记录
     */
    @GetMapping("/record/{recordId}")
    public Response<ExamRecord> getExamRecordById(@PathVariable("recordId") Long recordId) {
        try {
            ExamRecord record = examService.getExamRecordById(recordId);
            if (record != null) {
                return Response.success(record);
            } else {
                return Response.error("未找到指定的考试记录");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 更新考试信息 (使用POST方法作为主要接口)
     * @param examId 考试ID
     * @param req 更新请求参数
     * @return 是否更新成功
     */
    @PostMapping("/{examId}/update")
    public Response<Boolean> updateExamWithPost(@PathVariable("examId") Long examId,
                                             @RequestBody ExamUpdateReq req) {
        try {
            // 获取原始考试信息
            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                return Response.error("未找到指定的考试");
            }

            // 更新考试信息
            if (req.getTitle() != null) {
                exam.setTitle(req.getTitle());
            }
            if (req.getPaperId() != null) {
                exam.setPaperId(req.getPaperId());
            }
            if (req.getTeacherId() != null) {
                exam.setTeacherId(req.getTeacherId());
            }
            if (req.getStartTime() != null) {
                try {
                    Date startTime = SDF.parse(req.getStartTime());
                    exam.setStartTime(startTime);
                } catch (ParseException e) {
                    return Response.error("开始时间格式错误：请使用yyyy-MM-dd HH:mm:ss");
                }
            }
            if (req.getEndTime() != null) {
                try {
                    Date endTime = SDF.parse(req.getEndTime());
                    exam.setEndTime(endTime);
                } catch (ParseException e) {
                    return Response.error("结束时间格式错误：请使用yyyy-MM-dd HH:mm:ss");
                }
            }
            if (req.getExamCode() != null) {
                exam.setExamCode(req.getExamCode());
            }
            if (req.getStatus() != null) {
                exam.setStatus(req.getStatus());
            }

            // 调用服务更新考试
            boolean success = examService.updateExam(exam);
            return success ? Response.success(true) : Response.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新考试失败：" + e.getMessage());
        }
    }

    /**
     * 删除考试 (使用POST方法)
     * @param examId 考试ID
     * @return 是否删除成功
     */
    @PostMapping("/{examId}/delete")
    public Response<Boolean> deleteExamWithPost(@PathVariable("examId") Long examId) {
        try {
            boolean success = examService.deleteExam(examId);
            return success ? Response.success(true) : Response.error("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("删除考试失败：" + e.getMessage());
        }
    }

    /**
     * 为考试添加考生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否添加成功
     */
    @PostMapping("/{examId}/students")
    public Response<Boolean> addStudentsToExam(@PathVariable("examId") Long examId,
                                          @RequestBody List<Long> studentIds) {
        try {
            boolean success = examService.addStudentsToExam(examId, studentIds);
            return success ? Response.success(true) : Response.error("添加考生失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("添加考生失败：" + e.getMessage());
        }
    }

    /**
     * 从考试中移除考生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否移除成功
     */
    @DeleteMapping("/{examId}/students")
    public Response<Boolean> removeStudentsFromExam(@PathVariable("examId") Long examId,
                                               @RequestBody List<Long> studentIds) {
        try {
            boolean success = examService.removeStudentsFromExam(examId, studentIds);
            return success ? Response.success(true) : Response.error("移除考生失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("移除考生失败：" + e.getMessage());
        }
    }

    /**
     * 获取考试的所有考生
     * @param examId 考试ID
     * @return 考生列表
     */
    @GetMapping("/{examId}/students")
    public Response<List<ExamRecord>> getStudentsByExam(@PathVariable("examId") Long examId) {
        try {
            List<ExamRecord> records = examService.getStudentsByExam(examId);
            return Response.success(records);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询考生列表失败：" + e.getMessage());
        }
    }

    /**
     * 导出考试监考数据
     * @param examId 考试ID
     * @return CSV格式的监考数据
     */
    @GetMapping("/{examId}/export")
    public ResponseEntity<String> exportExamMonitoringData(@PathVariable("examId") Long examId) {
        try {
            List<ExamRecord> records = examService.getStudentsByExam(examId);
            
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("学生ID,客观题得分,主观题得分,总成绩,状态,交卷时间\n");
            
            // 添加具体数据
            for (ExamRecord record : records) {
                csvContent.append(record.getStudentId())
                        .append(",")
                        .append(record.getObjectiveScore() != null ? record.getObjectiveScore() : "")
                        .append(",")
                        .append(record.getSubjectiveScore() != null ? record.getSubjectiveScore() : "")
                        .append(",")
                        .append(record.getTotalScore() != null ? record.getTotalScore() : "")
                        .append(",")
                        .append(record.getStatus() != null ? record.getStatus() : "")
                        .append(",")
                        .append(record.getSubmitTime() != null ? record.getSubmitTime() : "")
                        .append("\n");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "exam_" + examId + "_monitoring_data.csv");
            
            return new ResponseEntity<>(csvContent.toString(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 请求参数类（创建考试）
     */
    public static class ExamCreateReq {
        // 考试基础字段（匹配exam表）
        private String title;
        private Long paperId;
        private Long teacherId;
        private String startTime;
        private String endTime;
        private String examCode;

        // 移除SQL中不存在的字段
        // private String batch;
        // private Integer session;

        // 考试设置字段（匹配exam_setting表）
        private Integer durationMinute;
        private Boolean allowLateEnter = true; // 默认允许迟到
        private Boolean questionShuffle = true; // 默认题目乱序
        private Boolean optionShuffle = true; // 默认选项乱序
        private Boolean preventScreenSwitch = false; // 默认不监控切屏
        private BigDecimal passingScore = new BigDecimal(60.0); // 默认及格分60
        private Boolean autoSubmit = true; // 默认超时自动提交
        private Boolean allowViewPaper = true; // 默认允许查看试卷
        private Boolean allowViewScore = true; // 默认允许查看成绩
        private BigDecimal multiChoicePartialRatio = new BigDecimal(0.5); // 默认多选题部分得分率
        
        // 防作弊设置字段
        private Boolean fillCaseSensitive = false; // 填空题是否区分大小写
        private Boolean fillIgnoreSymbols = true; // 填空题是否忽略符号
        private Boolean fillManualMark = false; // 填空题是否需要人工批改
        private Boolean peerReview = false; // 是否生生互评

        // 完整Getters & Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getPaperId() { return paperId; }
        public void setPaperId(Long paperId) { this.paperId = paperId; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getExamCode() { return examCode; }
        public void setExamCode(String examCode) { this.examCode = examCode; }
        public Integer getDurationMinute() { return durationMinute; }
        public void setDurationMinute(Integer durationMinute) { this.durationMinute = durationMinute; }
        public Boolean getAllowLateEnter() { return allowLateEnter; }
        public void setAllowLateEnter(Boolean allowLateEnter) { this.allowLateEnter = allowLateEnter; }
        public Boolean getQuestionShuffle() { return questionShuffle; }
        public void setQuestionShuffle(Boolean questionShuffle) { this.questionShuffle = questionShuffle; }
        public Boolean getOptionShuffle() { return optionShuffle; }
        public void setOptionShuffle(Boolean optionShuffle) { this.optionShuffle = optionShuffle; }
        public Boolean getPreventScreenSwitch() { return preventScreenSwitch; }
        public void setPreventScreenSwitch(Boolean preventScreenSwitch) { this.preventScreenSwitch = preventScreenSwitch; }
        public BigDecimal getPassingScore() { return passingScore; }
        public void setPassingScore(BigDecimal passingScore) { this.passingScore = passingScore; }
        public Boolean getAutoSubmit() { return autoSubmit; }
        public void setAutoSubmit(Boolean autoSubmit) { this.autoSubmit = autoSubmit; }
        public Boolean getAllowViewPaper() { return allowViewPaper; }
        public void setAllowViewPaper(Boolean allowViewPaper) { this.allowViewPaper = allowViewPaper; }
        public Boolean getAllowViewScore() { return allowViewScore; }
        public void setAllowViewScore(Boolean allowViewScore) { this.allowViewScore = allowViewScore; }
        public BigDecimal getMultiChoicePartialRatio() { return multiChoicePartialRatio; }
        public void setMultiChoicePartialRatio(BigDecimal multiChoicePartialRatio) { this.multiChoicePartialRatio = multiChoicePartialRatio; }
        
        // 防作弊设置字段的Getters & Setters
        public Boolean getFillCaseSensitive() { return fillCaseSensitive; }
        public void setFillCaseSensitive(Boolean fillCaseSensitive) { this.fillCaseSensitive = fillCaseSensitive; }
        public Boolean getFillIgnoreSymbols() { return fillIgnoreSymbols; }
        public void setFillIgnoreSymbols(Boolean fillIgnoreSymbols) { this.fillIgnoreSymbols = fillIgnoreSymbols; }
        public Boolean getFillManualMark() { return fillManualMark; }
        public void setFillManualMark(Boolean fillManualMark) { this.fillManualMark = fillManualMark; }
        public Boolean getPeerReview() { return peerReview; }
        public void setPeerReview(Boolean peerReview) { this.peerReview = peerReview; }
    }

    /**
     * 请求参数类（更新考试）
     */
    public static class ExamUpdateReq {
        private String title;
        private Long paperId;
        private Long teacherId;
        private String startTime;
        private String endTime;
        private String examCode;
        private Byte status;

        // Getters & Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getPaperId() { return paperId; }
        public void setPaperId(Long paperId) { this.paperId = paperId; }
        public Long getTeacherId() { return teacherId; }
        public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getExamCode() { return examCode; }
        public void setExamCode(String examCode) { this.examCode = examCode; }
        public Byte getStatus() { return status; }
        public void setStatus(Byte status) { this.status = status; }
    }
}