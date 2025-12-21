package cn.edu.zjut.backend.controller;

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
import java.util.List;
import java.util.Map;

@Controller
public class QuestionController {
    @Autowired
    @Qualifier("questionServ")
    private QuestionService questionServ;

    @RequestMapping(value = "/api/question", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<Questions>> addQuestion(@RequestBody Questions question) {
        if(questionServ.addQuestion(question)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/question/import", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<Questions>> importQuestion(@RequestBody List<Questions> questions) {
        if(questionServ.importQuestion(questions)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/question/file", method = RequestMethod.POST)
    @ResponseBody
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
    public Response<List<Questions>> queryQuestion(QuestionQueryDTO dto, Model model) {
        System.out.println("1");
        List<Questions> questions = questionServ.queryQuestion(dto);
        return Response.success(questions);
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.DELETE)
    @ResponseBody
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
