package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.service.QuestionService;
import cn.edu.zjut.backend.util.ContextAwareRunnable;
import cn.edu.zjut.backend.util.JedisConnectionFactory;
import cn.edu.zjut.backend.util.Response;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Controller
public class QuestionController {
    @Autowired
    @Qualifier("questionServ")
    private QuestionService questionServ;

    @RequestMapping(value = "/api/question", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "添加题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Questions>> addQuestion(@RequestBody Questions question) {
        if(questionServ.addQuestion(question)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/question/import", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "批量导入题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Questions>> importQuestion(@RequestBody List<Questions> questions) {
        if(questionServ.importQuestion(questions)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/question/file", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "文件导入题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<String> fileImportQuestionsAsync(@RequestBody Map<String, Object> request) {

        String fileContent = (String) request.get("file");
        if (fileContent == null || fileContent.isEmpty()) {
            return Response.error("文件内容为空");
        }

        String taskId = UUID.randomUUID().toString();

        CompletableFuture.runAsync(
            new ContextAwareRunnable(()->{
                try {
                    // 执行实际导入，并传入 taskId 用于更新进度
                    questionServ.fileImportQuestionsAsync(fileContent, taskId);
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
            }),Executors.newCachedThreadPool());

        return Response.success(taskId);
    }

    // 进度查询接口
    @RequestMapping(value = "/api/taskProgress/{taskId}", method = RequestMethod.GET)
    @ResponseBody
    public Response<Map<String, String>> getTaskProgress(@PathVariable String taskId) {
        String key = "task:" + taskId;
        Jedis jedis = null;
        try {
            jedis = JedisConnectionFactory.getJedis();
            Map<String, String> result = jedis.hgetAll(key);
            if (result == null || result.isEmpty()) {
                result = Map.of("status", "not_found");
            }
            return Response.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询进度失败");
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "查询题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<Map<String, Object>> queryQuestion(QuestionQueryDTO filterDTO, Model model) {
        List<Questions> questions = questionServ.queryQuestion(filterDTO);

        if(questions==null || questions.isEmpty() || filterDTO.getPageNum()==null || filterDTO.getPageSize()==null) {
            Map<String, Object> multiData = new HashMap<>();
            multiData.put("questions", questions);
            multiData.put("total", 0);

            return Response.success(multiData);
        }
        // 设置分页参数
        // 关键公式：开始索引 = (当前页 - 1) * 每页大小
        int firstResult = (filterDTO.getPageNum() - 1) * filterDTO.getPageSize();
        int total = (questions.size() + filterDTO.getPageSize() - 1) / filterDTO.getPageSize(); // 总页数

        // 1. 计算结束索引
        int toIndex = firstResult + filterDTO.getPageSize();

        if (toIndex > questions.size()) {
            toIndex = questions.size();
        }

        try{
            questions = questions.subList(firstResult, toIndex);
        }catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> multiData = new HashMap<>();
        multiData.put("questions", questions);
        multiData.put("total", total);

        return Response.success(multiData);

    }

    @RequestMapping(value = "/api/question-id", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "查询题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<Questions> queryQuestion(@RequestParam(required = false) Long questionId) {
        if(questionId==null){
            return Response.error("参数错误");
        }
        Questions question = questionServ.queryQuestion(questionId);
        return Response.success(question);
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "删除题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Questions>> deleteQuestion(@RequestBody List<Long> ids, Model model) {
        if(ids==null || ids.isEmpty()){
            return Response.error("未指定参数");
        }
        if(questionServ.deleteQuestion(ids)) {
            return Response.success();
        }else{
            return Response.error("删除失败");
        }
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "更新题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Questions>> updateQuestion(@RequestBody Questions questions, Model model) {
        if(questions==null || questions.getId()==null || (questions.getQuestionItems() == null && questions.getQuestionComponents() == null)){
            return Response.error("参数错误");
        }
        if(questionServ.updateQuestion(questions)) {
            return Response.success();
        }else{
            return Response.error("修改失败");
        }
    }
}