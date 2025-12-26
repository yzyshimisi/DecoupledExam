package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.QuestionType;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.service.QuestionTypeService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import cn.edu.zjut.backend.annotation.LogRecord;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class QuestionTypeController {
    @Autowired
    @Qualifier("questionTypeServ")
    private QuestionTypeService questionTypeServ;

    @RequestMapping(value = "/api/question_type", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "题型管理", action = "查询题型", targetType = "题型", logType = LogRecord.LogType.OPERATION)
    public Response<List<QuestionType>> queryQuestionTypes() {
        return Response.success(questionTypeServ.getAllQuestionTypes());
    }
}
