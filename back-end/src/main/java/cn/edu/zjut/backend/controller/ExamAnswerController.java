package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.dto.AddExamAnswerDTO;
import cn.edu.zjut.backend.dto.ExamPaperDTO;
import cn.edu.zjut.backend.service.ExamAnswerService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class ExamAnswerController {
    @Autowired
    @Qualifier("examAnswerServ")
    private ExamAnswerService examAnswerServ;

    @RequestMapping(value = "/api/examAnswer", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<ExamPaperDTO>> addExamAnswer(@RequestBody(required = false) AddExamAnswerDTO addExamAnswerDTO) {
        try{
            if(examAnswerServ.addExamAnswer(addExamAnswerDTO)){
                return Response.success();
            }else{
                return Response.error("提交失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            return Response.error(e.getMessage());
        }
    }
}
