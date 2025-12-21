package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.service.SubjectService;
import cn.edu.zjut.backend.util.Response;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class SubjectController {

    @Autowired
    @Qualifier("subjectServ")
    private SubjectService subjectServ;

    @RequestMapping(value = "/api/subject", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<Subject>> addSubject(@RequestBody Subject subject, Model model) {
        if (subjectServ.addSubject(subject)) {
            return Response.success();
        }
        else{
            return Response.error("添加失败");
        }
    }

    @RequestMapping(value = "/api/subject", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<Subject>> getSubject(@RequestParam(value="id", required=false) Integer id, Model model) {
        int subjectId = id==null ? -1 : id;
        List<Subject> subjects = subjectServ.getSubject(subjectId);
        return Response.success(subjects);
    }

    @RequestMapping(value = "/api/subject", method = RequestMethod.DELETE)
    @ResponseBody
    public Response<List<Subject>> deleteSubject(@RequestBody(required = false) DeleteSubjectReq req, Model model) {
        if(req==null || req.getId()==null){
            return Response.error("id参数不能为空");
        }
        int subjects = req.getId();
        if(subjectServ.deleteSubject(subjects)){
            return Response.success();
        }else{
            return Response.error("删除失败");
        }
    }
}

class DeleteSubjectReq {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}