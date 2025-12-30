package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.annotation.LogRecord;
import cn.edu.zjut.backend.po.Course;
import cn.edu.zjut.backend.po.StudentCourse;
import cn.edu.zjut.backend.dto.StudentCourseDetailDTO;
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
    @LogRecord(module = "课程管理", action = "创建课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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

            // ======== 问题1修复：添加参数验证 ========
            // 验证课程名称不能为空
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                return Response.error("课程名称不能为空");
            }
            
            // 验证学科ID不能为空
            if (course.getSubjectId() == null) {
                return Response.error("学科ID不能为空");
            }

            // 设置教师ID、创建时间和默认状态
            course.setTeacherId(userId);
            course.setCreateTime(new Date());
            course.setStatus("0"); // 默认状态为开设中

            // 创建课程
            String result = courseService.createCourse(course);
            if (result == null || result.isEmpty()) {
                return Response.success(course);
            } else {
                return Response.error(result);
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
    @LogRecord(module = "课程管理", action = "查看我的课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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
    @LogRecord(module = "课程管理", action = "根据邀请码查找课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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
    @LogRecord(module = "课程管理", action = "根据ID获取课程详情", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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
    @LogRecord(module = "课程管理", action = "学生加入课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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
    @LogRecord(module = "课程管理", action = "学生退出课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
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
    @LogRecord(module = "课程管理", action = "查看我加入的课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
    public Response<List<StudentCourseDetailDTO>> getMyJoinedCourses(HttpServletRequest request) {
        try {
            // 从request中获取学生ID
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            Long studentId = ((Number) claims.get("id")).longValue();

            // 查询该学生加入的所有课程
            List<StudentCourse> studentCourses = studentCourseService.getStudentCourses(studentId);
            // 将 StudentCourse 转换为包含 Course 详情的 DTO
            List<StudentCourseDetailDTO> details = new java.util.ArrayList<>();
            if (studentCourses != null) {
                for (StudentCourse sc : studentCourses) {
                    StudentCourseDetailDTO dto = new StudentCourseDetailDTO();
                    dto.setStudentId(sc.getStudentId());
                    dto.setCourseId(sc.getCourseId());
                    dto.setJoinTime(sc.getJoinTime());
                    dto.setStatus(sc.getStatus());
                    // 查询课程详情
                    Course course = courseService.findCourseById(sc.getCourseId());
                    dto.setCourse(course);
                    details.add(dto);
                }
            }
            return Response.success(details);
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 教师删除自己创建的课程
     */
    @RequestMapping(value = "/api/course/delete/{courseId}", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "课程管理", action = "删除课程", targetType = "课程", logType = LogRecord.LogType.OPERATION)
    public Response<String> deleteCourse(@PathVariable("courseId") Long courseId, HttpServletRequest request) {
        try {
            // 从request中获取用户信息
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            // 只有管理员和教师可以删除课程
            if (userType != 0 && userType != 1) {
                return Response.error("权限不足，只有管理员和教师可以删除课程");
            }
            
            // 教务老师可以删除所有课程，任课老师只能删除自己创建的课程
            if (userType == 1) { // 教师
                // 检查教师职位
                boolean isAcademicAffairsTeacher = teacherService.isAcademicAffairsTeacher(userId);
                if (!isAcademicAffairsTeacher) {
                    // 任课老师只能删除自己创建的课程
                    // deleteCourseById 方法会验证课程是否为该教师创建
                }
            }
            
            // 删除课程
            boolean success = courseService.deleteCourseById(courseId, userId);
            if (success) {
                return Response.success("成功删除课程");
            } else {
                return Response.error("删除课程失败，可能课程不存在或您没有权限删除该课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 教师修改自己创建的课程信息（课程名称和课程描述）
     */
    @PutMapping("/api/course/update/{courseId}")
    @ResponseBody
    @LogRecord(module = "课程管理", action = "更新课程信息", targetType = "课程", logType = LogRecord.LogType.OPERATION)
    public Response<String> updateCourse(@PathVariable("courseId") Long courseId, 
                                       @RequestBody Map<String, Object> requestBody, 
                                       HttpServletRequest request) {
        try {
            // 从request中获取用户信息
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            // 只有管理员和教师可以修改课程
            if (userType != 0 && userType != 1) {
                return Response.error("权限不足，只有管理员和教师可以修改课程");
            }
            
            // 从请求体获取新的课程名称和描述
            String courseName = (String) requestBody.get("courseName");
            String description = (String) requestBody.get("description");
            
            // 检查是否至少提供了一个要更新的字段
            if ((courseName == null || courseName.trim().isEmpty()) && description == null) {
                return Response.error("至少需要提供课程名称或课程描述中的一个");
            }
            
            // 教务老师可以修改所有课程，任课老师只能修改自己创建的课程
            if (userType == 1) { // 教师
                // 检查教师职位
                boolean isAcademicAffairsTeacher = teacherService.isAcademicAffairsTeacher(userId);
                if (!isAcademicAffairsTeacher) {
                    // 任课老师只能修改自己创建的课程
                    // updateCourseById 方法会验证课程是否为该教师创建
                }
            }
            
            // 更新课程
            boolean success = courseService.updateCourseById(courseId, userId, courseName, description);
            if (success) {
                return Response.success("成功更新课程信息");
            } else {
                return Response.error("更新课程信息失败，可能课程不存在或您没有权限修改该课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 教师修改课程状态（开设/结课）
     */
    @PutMapping("/api/course/status/{courseId}")
    @ResponseBody
    @LogRecord(module = "课程管理", action = "修改课程状态", targetType = "课程", logType = LogRecord.LogType.OPERATION)
    public Response<String> updateCourseStatus(@PathVariable("courseId") Long courseId, 
                                            @RequestBody Map<String, Object> requestBody, 
                                            HttpServletRequest request) {
        try {
            // 从request中获取用户信息
            Claims claims = (Claims) request.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }

            // 获取用户类型 0-管理员 1-教师 2-学生
            Integer userType = (Integer) claims.get("userType");
            Long userId = ((Number) claims.get("id")).longValue();
            
            // 只有管理员和教师可以修改课程状态
            if (userType != 0 && userType != 1) {
                return Response.error("权限不足，只有管理员和教师可以修改课程状态");
            }
            
            // 从请求体获取新的课程状态
            String status = (String) requestBody.get("status");
            
            // 验证状态值是否有效
            if (status == null || (!"0".equals(status) && !"1".equals(status))) {
                return Response.error("课程状态值无效，应为: 0=开设, 1=结课");
            }
            
            // 教务老师可以修改所有课程状态，任课老师只能修改自己创建的课程状态
            if (userType == 1) { // 教师
                // 检查教师职位
                boolean isAcademicAffairsTeacher = teacherService.isAcademicAffairsTeacher(userId);
                if (!isAcademicAffairsTeacher) {
                    // 任课老师只能修改自己创建的课程状态
                    // updateCourseStatus 方法会验证课程是否为该教师创建
                }
            }
            
            // 更新课程状态
            boolean success = courseService.updateCourseStatus(courseId, userId, status);
            if (success) {
                String statusText = "0".equals(status) ? "开设" : "结课";
                return Response.success("成功将课程状态更新为: " + statusText);
            } else {
                return Response.error("更新课程状态失败，可能课程不存在或您没有权限修改该课程");
            }
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取课程详情
     */
    @RequestMapping(value = "/api/course/{id}", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "课程管理", action = "获取课程详情", targetType = "课程", logType = LogRecord.LogType.OPERATION)
    public Response<Course> getCourseById(@PathVariable("id") Long courseId, HttpServletRequest request) {
        try {
            Course course = courseService.findCourseById(courseId);
            if (course == null) {
                return Response.error("未找到对应课程");
            }
            return Response.success(course);
        } catch (Exception e) {
            return Response.error("服务器内部错误: " + e.getMessage());
        }
    }
}