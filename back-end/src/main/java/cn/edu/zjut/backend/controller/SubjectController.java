package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.service.SubjectService;
import cn.edu.zjut.backend.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SubjectController {

    @Autowired
    @Qualifier("subjectServ")
    private SubjectService subjectServ;

    @RequestMapping(value = "/api/subject", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "科目管理", action = "添加科目", targetType = "科目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Subject>> addSubject(@RequestBody Subject subject, Model model) {
        if (subjectServ.addSubject(subject)) {
            return Response.success();
        }
        else{
            return Response.error("添加失败");
        }
    }

    @RequestMapping(value = "/api/subject/import", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "科目管理", action = "批量导入科目", targetType = "科目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Subject>> addSubjects(@RequestBody List<Subject> subjects, HttpServletRequest res, Model model) {
        Claims claims = (Claims) res.getAttribute("claims");
        System.out.println("claims:" + claims);
        if (subjectServ.addSubject(subjects)) {
            return Response.success();
        }
        else{
            return Response.error("添加失败");
        }
    }

    @RequestMapping(value = "/api/subject", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "科目管理", action = "查询科目", targetType = "科目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Subject>> getSubject(@RequestParam(value="id", required=false) Integer id, HttpServletRequest res) {
        Claims claims = (Claims) res.getAttribute("claims");
        System.out.println("claims:" + claims.get("id") + " " + claims.get("username") + " " + claims.get("userType"));

        int subjectId = id==null ? -1 : id;
        List<Subject> subjects = subjectServ.getSubject(subjectId);
        return Response.success(subjects);
    }

    @RequestMapping(value = "/api/subject", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "科目管理", action = "删除科目", targetType = "科目", logType = LogRecord.LogType.OPERATION)
    public Response<List<Subject>> deleteSubject(@RequestBody(required = false) List<Integer> ids, Model model) {
        if(ids==null || ids.isEmpty()){
            return Response.error("id参数不能为空");
        }
        if(subjectServ.deleteSubject(ids)){
            return Response.success();
        }else{
            return Response.error("删除失败");
        }
    }
}