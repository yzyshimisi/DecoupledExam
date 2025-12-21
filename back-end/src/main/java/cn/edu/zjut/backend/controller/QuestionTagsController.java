package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.QuestionTags;
import cn.edu.zjut.backend.service.QuestionTagsService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class QuestionTagsController {
    @Autowired
    @Qualifier("questionTagsServ")
    private QuestionTagsService questionTagsServ;

    @RequestMapping(value = "/api/question/tags", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<QuestionTags>> addQuestionTags(@RequestBody List<QuestionTags> questionTagsList, Model model) {
        if(questionTagsServ.addQuestionTags(questionTagsList)) {
            return Response.success();
        }else{
            return Response.error("题目标签添加失败");
        }
    }

    @RequestMapping(value = "/api/question/tags", method = RequestMethod.DELETE)
    @ResponseBody
    public Response<List<QuestionTags>> deleteQuestionTags(@RequestBody List<Long> ids, Model model) {
        if(questionTagsServ.deleteQuestionTags(ids)) {
            return Response.success();
        }else{
            return Response.error("题目标签删除失败");
        }
    }

    @RequestMapping(value = "/api/question/tags", method = RequestMethod.PUT)
    @ResponseBody
    public Response<List<QuestionTags>> updateQuestionTags(@RequestBody QuestionTags questionTags, Model model) {
        if(questionTagsServ.updateQuestionTags(questionTags)) {
            return Response.success();
        }else{
            return Response.error("题目标签修改失败");
        }
    }

    @RequestMapping(value = "/api/question/tags", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<QuestionTags>> queryQuestionTags(@RequestParam(value = "questionId", required = false) Long questionId, Model model) {
        if(questionId==null){
            return Response.error("参数禁止为空");
        }

        return Response.success(questionTagsServ.queryQuestionTags(questionId));
    }
}
