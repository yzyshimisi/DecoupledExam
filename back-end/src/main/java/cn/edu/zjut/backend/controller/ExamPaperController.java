package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dao.ExamPaperDAO;
import cn.edu.zjut.backend.dto.ExamGenerationRequest;
import cn.edu.zjut.backend.dto.ExamPaperDTO;
import cn.edu.zjut.backend.po.ExamPaper;
import cn.edu.zjut.backend.po.ExamPaperQuestion;
import cn.edu.zjut.backend.po.ExamPaperQuestionId;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.service.ExamPaperService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ExamPaperController {

    @Autowired
    @Qualifier("examPaperServ")
    private ExamPaperService examPaperServ;

    @RequestMapping(value = "/api/examPaper", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "添加试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> addExamPaper(@RequestBody ExamPaperDTO examPaperDTO, Model model) {
        if(examPaperServ.addExamPaper(examPaperDTO)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/examPaper/generate", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "生成试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> generateExamPaper(@RequestBody ExamGenerationRequest examGenerationRequest , Model model) {
        if(examPaperServ.generateExamPaper(examGenerationRequest)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/examPaper", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "查询试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> queryExamPaper(@RequestParam(value="creatorId", required = false) Long creatorId, Model model) {
        List<ExamPaperDTO> examPaperDTOS = examPaperServ.queryExamPaper(creatorId);
        return Response.success(examPaperDTOS);
    }

    @RequestMapping(value = "/api/examPaper", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "删除试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> deleteExamPaper(@RequestBody(required = false) List<Long> paperIds, Model model) {
       if(paperIds == null || paperIds.isEmpty()) {
           return Response.error("id参数不能为空");
       }

       if(examPaperServ.deleteExamPaper(paperIds)) {
           return Response.success();
       }else{
           return Response.error("删除失败");
       }
    }

    @RequestMapping(value = "/api/examPaper", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "更新试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> updateExamPaper(@RequestBody(required = false) ExamPaper examPaper, Model model) {
        if(examPaper == null) {
            return Response.error("参数不能为空");
        }

        if(examPaperServ.updateExamPaper(examPaper)) {
            return Response.success();
        }else{
            return Response.error("删除失败");
        }
    }

    @RequestMapping(value = "/api/examPaper/question", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "添加试卷题目", targetType = "试卷题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> addExamPaperQuestion(@RequestBody(required = false) List<ExamPaperQuestion> examPaperQuestions, Model model) {
        if(examPaperServ.addExamPaperQuestion(examPaperQuestions)) {
            return Response.success();
        }else{
            return Response.error("添加试卷题目失败");
        }
    }

    @RequestMapping(value = "/api/examPaper/question", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "删除试卷题目", targetType = "试卷题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> deleteExamPaperQuestion(@RequestBody(required = false)List<ExamPaperQuestionId> examPaperQuestionIds , Model model) {
        if(examPaperServ.deleteExamPaperQuestion(examPaperQuestionIds)) {
            return Response.success();
        }else{
            return Response.error("删除试卷题目失败");
        }
    }

    @RequestMapping(value = "/api/examPaper/question", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "更新试卷题目", targetType = "试卷题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> updateExamPaperQuestion(@RequestBody(required = false) ExamPaperQuestion examPaperQuestion, Model model) {
        if(examPaperServ.updateExamPaperQuestion(examPaperQuestion)) {
            return Response.success();
        }else{
            return Response.error("修改试卷题目失败");
        }
    }
}