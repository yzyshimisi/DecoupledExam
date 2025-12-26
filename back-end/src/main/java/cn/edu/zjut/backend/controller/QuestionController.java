package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.service.QuestionService;
import cn.edu.zjut.backend.util.Response;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Response<List<Questions>> fileImportQuestion(@RequestBody Map<String, Object> request) {

        String fileContent = (String) request.get("file");
        if (fileContent == null || fileContent.isEmpty()) {
            return Response.error("文件内容为空");
        }

        if(questionServ.fileImportQuestions(fileContent)) {
            return Response.success();
        } else {
            return Response.error("导入失败");
        }
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "题库管理", action = "查询题目", targetType = "题目", logType = LogRecord.LogType.OPERATION)
    public Response<Map<String, Object>> queryQuestion(QuestionQueryDTO filterDTO, Model model) {
        List<Questions> questions = questionServ.queryQuestion(filterDTO);
        if(questions==null || questions.isEmpty()) {
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
        System.out.println(firstResult);

        Map<String, Object> multiData = new HashMap<>();
        multiData.put("questions", questions);
        multiData.put("total", total);

        return Response.success(multiData);

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