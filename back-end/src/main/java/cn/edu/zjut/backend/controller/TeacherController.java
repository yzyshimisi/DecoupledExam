package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.TeacherPosition;
import cn.edu.zjut.backend.po.TeacherSubject;
import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.service.TeacherService;
import cn.edu.zjut.backend.util.Jwt;
import cn.edu.zjut.backend.util.Response;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    @Qualifier("teacherServ")
    private TeacherService teacherService;

    // 用于接收教师职位设置的请求体
    public static class PositionRequest {
        private Long teacherId;
        private Byte role;

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public Byte getRole() {
            return role;
        }

        public void setRole(Byte role) {
            this.role = role;
        }
    }

    // 用于接收教师学科绑定的请求体
    public static class SubjectRequest {
        private Long teacherId;
        private Integer subjectId;
        private Byte isMain;

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public Integer getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(Integer subjectId) {
            this.subjectId = subjectId;
        }

        public Byte getIsMain() {
            return isMain;
        }

        public void setIsMain(Byte isMain) {
            this.isMain = isMain;
        }
    }

    /**
     * 设置教师职位（仅管理员）
     * @param request 请求体，包含教师ID和职位
     * @return 操作结果
     */
    @PostMapping("/position")
    @ResponseBody
    public Response<String> setTeacherPosition(
            @RequestBody PositionRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 验证是否为管理员
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Integer currentUserType = (Integer) claims.get("userType");
            if (currentUserType != 0) { // 0=管理员
                return Response.error("权限不足，仅管理员可执行此操作");
            }
            
            boolean result = teacherService.setTeacherPosition(request.getTeacherId(), request.getRole());
            if (result) {
                return Response.success("教师职位设置成功");
            } else {
                return Response.error("教师职位设置失败，教师不存在或不是教师账号");
            }
        } catch (Exception e) {
            return Response.error("教师职位设置异常：" + e.getMessage());
        }
    }

    /**
     * 获取教师职位（仅管理员）
     * @param teacherId 教师ID
     * @return 教师职位信息
     */
    @GetMapping("/position")
    @ResponseBody
    public Response<TeacherPosition> getTeacherPosition(
            @RequestParam("teacherId") Long teacherId,
            HttpServletRequest httpRequest) {
        try {
            // 验证是否为管理员
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Integer currentUserType = (Integer) claims.get("userType");
            if (currentUserType != 0) { // 0=管理员
                return Response.error("权限不足，仅管理员可执行此操作");
            }
            
            TeacherPosition position = teacherService.getTeacherPosition(teacherId);
            if (position != null) {
                return Response.success(position);
            } else {
                return Response.error("未找到该教师的职位信息");
            }
        } catch (Exception e) {
            return Response.error("获取教师职位信息异常：" + e.getMessage());
        }
    }

    /**
     * 根据职位查询教师列表（仅管理员）
     * @param role 职位：0=任课老师，1=教务老师
     * @return 符合条件的教师职位列表
     */
    @GetMapping("/position/list")
    @ResponseBody
    public Response<List<TeacherPosition>> getTeachersByRole(
            @RequestParam("role") Byte role,
            HttpServletRequest httpRequest) {
        try {
            // 验证是否为管理员
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Integer currentUserType = (Integer) claims.get("userType");
            if (currentUserType != 0) { // 0=管理员
                return Response.error("权限不足，仅管理员可执行此操作");
            }
            
            List<TeacherPosition> positions = teacherService.getTeachersByRole(role);
            return Response.success(positions);
        } catch (Exception e) {
            return Response.error("查询教师列表异常：" + e.getMessage());
        }
    }

    /**
     * 绑定教师与学科
     * 教师只能绑定自己的学科，管理员可以绑定任意教师的学科
     * @param request 请求体，包含教师ID、学科ID和是否主讲
     * @return 操作结果
     */
    @PostMapping("/subject")
    @ResponseBody
    public Response<String> bindTeacherSubject(
            @RequestBody SubjectRequest request,
            HttpServletRequest httpRequest) {
        try {
            // 验证权限：教师只能操作自己的账号，管理员可以操作所有账号
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Long currentUserId = ((Number) claims.get("id")).longValue();
            Integer currentUserType = (Integer) claims.get("userType");
            
            Long teacherId = request.getTeacherId();
            Integer subjectId = request.getSubjectId();
            
            // 如果是教师，只能操作自己的账号
            if (currentUserType == 1 && !currentUserId.equals(teacherId)) { // 1=教师
                return Response.error("权限不足，教师只能操作自己的账号");
            }
            
            // 如果是学生，无权操作
            if (currentUserType == 2) { // 2=学生
                return Response.error("权限不足，学生无法执行此操作");
            }
            
            boolean result = teacherService.bindTeacherSubject(teacherId, subjectId, request.getIsMain());
            if (result) {
                return Response.success("教师学科绑定成功");
            } else {
                return Response.error("教师学科绑定失败，教师不存在或不是教师账号");
            }
        } catch (Exception e) {
            return Response.error("教师学科绑定异常：" + e.getMessage());
        }
    }

    /**
     * 获取教师的所有学科关联
     * 教师只能查看自己的学科，管理员可以查看所有教师的学科
     * @param teacherId 教师ID
     * @return 教师学科关联列表
     */
    @GetMapping("/subject")
    @ResponseBody
    public Response<List<TeacherSubject>> getTeacherSubjects(
            @RequestParam("teacherId") Long teacherId,
            HttpServletRequest httpRequest) {
        try {
            // 验证权限：教师只能查看自己的学科，管理员可以查看所有教师的学科
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Long currentUserId = ((Number) claims.get("id")).longValue();
            Integer currentUserType = (Integer) claims.get("userType");
            
            // 如果是教师，只能查看自己的学科
            if (currentUserType == 1 && !currentUserId.equals(teacherId)) { // 1=教师
                return Response.error("权限不足，教师只能查看自己的学科信息");
            }
            
            List<TeacherSubject> subjects = teacherService.getTeacherSubjects(teacherId);
            return Response.success(subjects);
        } catch (Exception e) {
            return Response.error("获取教师学科信息异常：" + e.getMessage());
        }
    }

    /**
     * 获取学科的所有教师关联（公开接口，所有人都可以查看）
     * @param subjectId 学科ID
     * @return 教师学科关联列表
     */
    @GetMapping("/subject/teachers")
    @ResponseBody
    public Response<List<TeacherSubject>> getSubjectTeachers(@RequestParam("subjectId") Integer subjectId) {
        try {
            List<TeacherSubject> teachers = teacherService.getSubjectTeachers(subjectId);
            return Response.success(teachers);
        } catch (Exception e) {
            return Response.error("获取学科教师信息异常：" + e.getMessage());
        }
    }

    /**
     * 解绑教师与学科
     * 教师只能解绑自己的学科，管理员可以解绑任意教师的学科
     * @param teacherId 教师ID
     * @param subjectId 学科ID
     * @return 操作结果
     */
    @DeleteMapping("/subject")
    @ResponseBody
    public Response<String> unbindTeacherSubject(
            @RequestParam("teacherId") Long teacherId,
            @RequestParam("subjectId") Integer subjectId,
            HttpServletRequest httpRequest) {
        try {
            // 验证权限：教师只能操作自己的账号，管理员可以操作所有账号
            Claims claims = (Claims) httpRequest.getAttribute("claims");
            if (claims == null) {
                return Response.error("用户未登录");
            }
            
            Long currentUserId = ((Number) claims.get("id")).longValue();
            Integer currentUserType = (Integer) claims.get("userType");
            
            // 如果是教师，只能操作自己的账号
            if (currentUserType == 1 && !currentUserId.equals(teacherId)) { // 1=教师
                return Response.error("权限不足，教师只能操作自己的账号");
            }
            
            // 如果是学生，无权操作
            if (currentUserType == 2) { // 2=学生
                return Response.error("权限不足，学生无法执行此操作");
            }
            
            boolean result = teacherService.unbindTeacherSubject(teacherId, subjectId);
            if (result) {
                return Response.success("教师学科解绑成功");
            } else {
                return Response.error("教师学科解绑失败");
            }
        } catch (Exception e) {
            return Response.error("教师学科解绑异常：" + e.getMessage());
        }
    }

    /**
     * 获取主教学科的教师列表（公开接口，所有人都可以查看）
     * @return 主教学科的教师列表
     */
    @GetMapping("/subject/main")
    @ResponseBody
    public Response<List<TeacherSubject>> getMainTeachers() {
        try {
            List<TeacherSubject> teachers = teacherService.getMainTeachers();
            return Response.success(teachers);
        } catch (Exception e) {
            return Response.error("获取主讲教师列表异常：" + e.getMessage());
        }
    }
}