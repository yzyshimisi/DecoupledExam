package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.service.QuestionService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class QuestionController {
    @Autowired
    @Qualifier("questionServ")
    private QuestionService questionServ;

    @RequestMapping(value = "/api/question", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<Questions>> addSubject(@RequestBody Questions question, Model model) {
        if(questionServ.addQuestion(question)) {
            return Response.success();
        }else{
            return Response.error("题目添加失败");
        }
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<Questions>> querySubject(QuestionQueryDTO dto, Model model) {
        List<Questions> questions = questionServ.queryQuestion(dto);
        return Response.success(questions);
    }

    @RequestMapping(value = "/api/question", method = RequestMethod.DELETE)
    @ResponseBody
    public Response<List<Questions>> deleteSubject(@RequestBody List<Long> ids, Model model) {
        if(ids==null || ids.isEmpty()){
            return Response.error("未指定参数");
        }
        if(questionServ.deleteQuestion(ids)) {
            return Response.success();
        }else{
            return Response.success();
        }
    }
}
