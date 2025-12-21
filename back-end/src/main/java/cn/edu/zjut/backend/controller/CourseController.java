package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.Course;
import cn.edu.zjut.backend.po.StudentCourse;
import cn.edu.zjut.backend.service.CourseService;
import cn.edu.zjut.backend.service.StudentCourseService;
import cn.edu.zjut.backend.service.TeacherService;
import cn.edu.zjut.backend.util.Response;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class CourseController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private StudentCourseService studentCourseService;
    
    @Autowired
    private TeacherService teacherService;

    /**
     * 教师创建课程
     */
    @RequestMapping(value = "/api/course/create", method = RequestMethod.POST)
    @ResponseBody
    public Response<Course> createCourse(@RequestBody Course course, HttpServletRequest request) {
        try {
            // 从request中获取用户信息（在实际应用中应该从JWT token中解析）
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            // 只有管理员和教师可以创建课程
            if (userType != 0 && userType != 1) {
                return Response.error("权限不足，只有管理员和教师可以创建课程");
            }

            // 设置教师ID、创建时间和默认状态
            course.setTeacherId(userId);
            course.setCreateTime(new Date());
            course.setStatus("0"); // 默认状态为开设中

            // 创建课程
            boolean success = courseService.createCourse(course);
            if (success) {
                return Response.success(course);
            } else {
                return Response.error("课程创建失败");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 教师查看自己创建的所有课程
     * 教务老师可以查看所有课程，任课老师只能查看自己创建的课程
     */
    @RequestMapping(value = "/api/course/my", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<Course>> getMyCourses(HttpServletRequest request) {
        try {
            // 从request中获取用户信息
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            List<Course> courses;
            // 管理员可以查看所有课程
            if (userType == 0) {
                courses = courseService.getAllCourses();
            } 
            // 教师根据职位区分权限
            else if (userType == 1) {
                // 检查教师职位
                boolean isAcademicAffairsTeacher = teacherService.isAcademicAffairsTeacher(userId);
                if (isAcademicAffairsTeacher) {
                    // 教务老师可以查看所有课程
                    courses = courseService.getAllCourses();
                } else {
                    // 任课老师只能查看自己创建的课程
                    courses = courseService.getCoursesByTeacher(userId);
                }
            } else {
                return Response.error("权限不足，只有管理员和教师可以查看课程列表");
            }
            
            return Response.success(courses);
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 根据邀请码查找课程信息
     */
    @RequestMapping(value = "/api/course/invite/{inviteCode}", method = RequestMethod.GET)
    @ResponseBody
    public Response<Course> getCourseByInviteCode(@PathVariable("inviteCode") String inviteCode) {
        try {
            Course course = courseService.findCourseByInviteCode(inviteCode);
            if (course != null) {
                return Response.success(course);
            } else {
                return Response.error("未找到对应邀请码的课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 根据课程ID获取课程详情
     */
    @RequestMapping(value = "/api/course/{courseId}", method = RequestMethod.GET)
    @ResponseBody
    public Response<Course> getCourseById(@PathVariable("courseId") Long courseId) {
        try {
            Course course = courseService.findCourseById(courseId);
            if (course != null) {
                return Response.success(course);
            } else {
                return Response.error("未找到对应ID的课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 学生通过邀请码加入课程（公开接口）
     * 支持JSON格式请求体
     */
    @RequestMapping(value = "/api/course/join", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> joinCourse(@RequestBody Map<String, String> requestBody,
                                       HttpServletRequest request) {
        try {
            // 从request中获取学生ID（在实际应用中应该从JWT token中解析）
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 从JSON请求体获取邀请码
            String inviteCode = requestBody.get("inviteCode");

            // 检查邀请码是否为空
            if (inviteCode == null || inviteCode.isEmpty()) {
                return Response.error("邀请码不能为空");
            }

            Long studentId = ((Number) claims.get("id")).longValue();

            // 加入课程
            boolean success = studentCourseService.joinCourseByInviteCode(studentId, inviteCode);
            if (success) {
                return Response.success("成功加入课程");
            } else {
                return Response.error("加入课程失败，邀请码无效或已加入该课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 学生退出课程
     */
    @RequestMapping(value = "/api/course/quit/{courseId}", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> quitCourse(@PathVariable("courseId") Long courseId, HttpServletRequest request) {
        try {
            // 从request中获取学生ID
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            Long studentId = ((Number) claims.get("id")).longValue();

            // 退出课程
            boolean success = studentCourseService.quitCourse(studentId, courseId);
            if (success) {
                return Response.success("成功退出课程");
            } else {
                return Response.error("退出课程失败，可能未加入该课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 学生查看自己加入的所有课程
     */
    @RequestMapping(value = "/api/course/my/joined", method = RequestMethod.GET)
    @ResponseBody
    public Response<List<StudentCourse>> getMyJoinedCourses(HttpServletRequest request) {
        try {
            // 从request中获取学生ID
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            Long studentId = ((Number) claims.get("id")).longValue();

            // 查询该学生加入的所有课程
            List<StudentCourse> studentCourses = studentCourseService.getStudentCourses(studentId);
            return Response.success(studentCourses);
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }
}