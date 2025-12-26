package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.po.StudentGrade;
import cn.edu.zjut.backend.service.StudentGradeService;
import cn.edu.zjut.backend.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/student-grades")
public class StudentGradeController {

    @Autowired
    private StudentGradeService studentGradeService;

    /**
     * 添加成绩
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "添加成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<Void> addStudentGrade(@RequestBody StudentGrade studentGrade) {
        try {
            System.out.println("接收到添加成绩请求: " + studentGrade.toString());
            boolean result = studentGradeService.addStudentGrade(studentGrade);
            if (result) {
                return Response.success();
            } else {
                return Response.error("添加成绩失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("添加成绩异常：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取成绩
     */
    @RequestMapping(value = "/{gradeId}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "根据ID获取成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<StudentGrade> getStudentGradeById(@PathVariable Long gradeId) {
        try {
            StudentGrade grade = studentGradeService.getStudentGradeById(gradeId);
            if (grade != null) {
                return Response.success(grade);
            } else {
                return Response.error("未找到指定成绩");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询成绩异常：" + e.getMessage());
        }
    }

    /**
     * 获取所有成绩
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "获取所有成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<List<StudentGrade>> getAllStudentGrades() {
        try {
            List<StudentGrade> grades = studentGradeService.getAllStudentGrades();
            return Response.success(grades);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询成绩异常：" + e.getMessage());
        }
    }

    /**
     * 根据学生ID获取成绩
     */
    @RequestMapping(value = "/student/{studentId}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "根据学生ID获取成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<List<StudentGrade>> getStudentGradesByStudentId(@PathVariable Long studentId) {
        try {
            List<StudentGrade> grades = studentGradeService.getStudentGradesByStudentId(studentId);
            return Response.success(grades);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询学生成绩异常：" + e.getMessage());
        }
    }

    /**
     * 根据课程ID获取成绩
     */
    @RequestMapping(value = "/course/{courseId}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "根据课程ID获取成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<List<StudentGrade>> getStudentGradesByCourseId(@PathVariable Long courseId) {
        try {
            List<StudentGrade> grades = studentGradeService.getStudentGradesByCourseId(courseId);
            return Response.success(grades);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询课程成绩异常：" + e.getMessage());
        }
    }

    /**
     * 根据成绩类型获取成绩
     */
    @RequestMapping(value = "/type/{gradeType}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "根据成绩类型获取成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<List<StudentGrade>> getStudentGradesByGradeType(@PathVariable String gradeType) {
        try {
            List<StudentGrade> grades = studentGradeService.getStudentGradesByGradeType(gradeType);
            return Response.success(grades);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询成绩异常：" + e.getMessage());
        }
    }

    /**
     * 更新成绩
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "更新成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<Void> updateStudentGrade(@RequestBody StudentGrade studentGrade) {
        try {
            boolean result = studentGradeService.updateStudentGrade(studentGrade);
            if (result) {
                return Response.success();
            } else {
                return Response.error("更新成绩失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("更新成绩异常：" + e.getMessage());
        }
    }

    /**
     * 删除成绩
     */
    @RequestMapping(value = "/{gradeId}", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "成绩管理", action = "删除成绩", targetType = "成绩", logType = LogRecord.LogType.OPERATION)
    public Response<Void> deleteStudentGrade(@PathVariable Long gradeId) {
        try {
            boolean result = studentGradeService.deleteStudentGrade(gradeId);
            if (result) {
                return Response.success();
            } else {
                return Response.error("删除成绩失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("删除成绩异常：" + e.getMessage());
        }
    }
}