package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dao.ExamPaperDAO;
import cn.edu.zjut.backend.dto.ExamGenerationRequest;
import cn.edu.zjut.backend.dto.ExamPaperDTO;
import cn.edu.zjut.backend.dto.ExamPaperQueryDTO;
import cn.edu.zjut.backend.po.ExamPaper;
import cn.edu.zjut.backend.po.ExamPaperQuestion;
import cn.edu.zjut.backend.po.ExamPaperQuestionId;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.service.ExamPaperService;
import cn.edu.zjut.backend.util.ContextAwareRunnable;
import cn.edu.zjut.backend.util.JedisConnectionFactory;
import cn.edu.zjut.backend.util.Response;
import cn.edu.zjut.backend.util.UserContext;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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
    public Response<String> generateExamPaperAsync(@RequestBody ExamGenerationRequest examGenerationRequest , Model model) {

        String taskId = UUID.randomUUID().toString();

        CompletableFuture.runAsync(
            new ContextAwareRunnable(()->{
                try {
                    // 执行实际导入，并传入 taskId 用于更新进度
                    examPaperServ.generateExamPaperAsync(examGenerationRequest, taskId);
                } catch (Exception e) {
                    // 记录失败
                    Jedis jedis = null;
                    try {
                        jedis = JedisConnectionFactory.getJedis();
                        jedis.hset("task:" + taskId, "status", "failed");
                        jedis.hset("task:" + taskId, "message", "系统异常: " + e.getMessage());
                        jedis.hset("task:" + taskId, "progress", "-1");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (jedis != null) jedis.close();
                    }
                    e.printStackTrace();
                }
            }), Executors.newCachedThreadPool());

        return Response.success(taskId);
    }

    @RequestMapping(value = "/api/examPaper/generate/progress/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public Response<Map<String, String>> getGenerateProgress(@PathVariable String taskId) {
        Map<String, String> progress = examPaperServ.getGenerateProgress(taskId);
        return Response.success(progress);
    }

    @RequestMapping(value = "/api/examPaper", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "查询试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<List<ExamPaperDTO>> queryExamPaper(@RequestParam(value="creatorId", required = false) Long creatorId, Model model) {
        List<ExamPaperDTO> examPaperDTOS = examPaperServ.queryExamPaper(UserContext.getUserId());
        return Response.success(examPaperDTOS);
    }

    // 学生获取考试的试卷（验证过滤）
    @RequestMapping(value = "/api/exam-paper/{examId}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "查询试卷", targetType = "试卷", logType = LogRecord.LogType.OPERATION)
    public Response<ExamPaperDTO> queryExamPaper(@PathVariable Long examId) {
        try{
            ExamPaperDTO examPaperDTO = examPaperServ.queryExamPaperByExamId(examId);

            if(examPaperDTO==null){
                return Response.error("试卷获取失败");
            }else{
                return Response.success(examPaperDTO);
            }
        }catch (Exception e){
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
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
            return Response.error("修改失败");
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
    public Response<List<ExamPaperDTO>> updateExamPaperQuestion(@RequestBody(required = false) List<ExamPaperQuestion> examPaperQuestions, Model model) {
        if(examPaperServ.updateExamPaperQuestion(examPaperQuestions)) {
            return Response.success();
        }else{
            return Response.error("修改试卷题目失败");
        }
    }

    @RequestMapping(value = "/api/examPaper/seal", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "试卷管理", action = "更新试卷题目", targetType = "试卷题目", logType = LogRecord.LogType.OPERATION)
    public Response<String> modifySealedStatus(@RequestBody List<Long> paperIds) {
        if(examPaperServ.modifySealedStatus(paperIds)) {
            return Response.success();
        }else{
            return Response.error("修改试卷题目失败");
        }
    }
}