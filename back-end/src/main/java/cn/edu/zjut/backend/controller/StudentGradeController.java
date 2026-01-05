package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.StudentGrade;
import cn.edu.zjut.backend.service.StudentGradeService;
import cn.edu.zjut.backend.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
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

    /**
     * 获取当前教师负责课程下的所有学生成绩
     */
    @RequestMapping(value = "/teacher", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<StudentGrade>> getStudentGradesByTeacher(HttpServletRequest request) {
        try {
            // 从request中获取用户信息（在实际应用中应该从JWT token中解析）
            Object claimsObj = request.getAttribute("claims");
            if (claimsObj == null) {
                return Response.error("用户未登录");
            }
            
            // 类型检查
            if (!(claimsObj instanceof Claims)) {
                return Response.error("用户信息格式错误");
            }
            
            Claims claims = (Claims) claimsObj;
            
            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            // 只有教师可以查看自己负责课程的成绩
            if (userType != 1) {
                return Response.error("权限不足，只有教师可以查看成绩");
            }

            List<StudentGrade> grades = studentGradeService.getStudentGradesByTeacher(userId);
            return Response.success(grades);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("查询教师成绩异常：" + e.getMessage());
        }
    }
}