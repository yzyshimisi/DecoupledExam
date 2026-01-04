package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.CourseDAO;
import cn.edu.zjut.backend.po.Course;
import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("courseServ")
public class CourseService {
    
    @Autowired
    private TeacherService teacherService;
    
    public Session getSession() {
        return HibernateUtil.getSession();
    }
    
    /**
     * 创建课程
     * @param course 课程信息
     * @return null或空字符串表示成功，非空字符串表示错误信息
     */
    public String createCourse(Course course) {
        Session session = null;
        Transaction tran = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            // ======== 问题2修复：验证subject是否存在 ========
            if (course.getSubjectId() != null) {
                // 查询subject是否存在
                String subjectHql = "FROM Subject WHERE subjectId = :subjectId";
                org.hibernate.query.Query<Object> subjectQuery = session.createQuery(subjectHql);
                subjectQuery.setParameter("subjectId", course.getSubjectId());
                if (subjectQuery.list().isEmpty()) {
                    return "指定的学科ID不存在，请检查subjectId: " + course.getSubjectId();
                }
            }
            
            // 生成邀请码
            String inviteCode = generateInviteCode();
            course.setInviteCode(inviteCode);
            
            tran = session.beginTransaction();
            dao.add(course);
            tran.commit();
            return ""; // 空字符串表示成功
        } catch (org.hibernate.exception.ConstraintViolationException cve) {
            // ======== 问题3修复：处理约束违反异常 ========
            System.out.println("Database constraint violation when creating course: " + cve.getMessage());
            cve.printStackTrace();
            if (tran != null) {
                tran.rollback();
            }
            // 判断是哪个约束违反
            String errorMsg = cve.getCause() != null ? cve.getCause().getMessage() : cve.getMessage();
            if (errorMsg.contains("fk_course_subject") || errorMsg.contains("subject_id")) {
                return "课程所属学科不存在，请确保subjectId存在于subject表中";
            } else if (errorMsg.contains("fk_course_teacher") || errorMsg.contains("teacher_id")) {
                return "教师ID不存在";
            } else if (errorMsg.contains("uk_invite_code") || errorMsg.contains("invite_code")) {
                return "邀请码生成冲突，请重试";
            }
            return "数据库约束违反: " + errorMsg;
        } catch (Exception e) {
            System.out.println("create course failed " + e);
            e.printStackTrace(); // 打印详细异常信息
            if (tran != null) {
                tran.rollback();
            }
            return "课程创建失败: " + e.getMessage();
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 根据邀请码查找课程
     * @param inviteCode 邀请码
     * @return 课程信息，找不到返回null
     */
    public Course findCourseByInviteCode(String inviteCode) {
        Session session = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            return dao.findByInviteCode(inviteCode);
        } catch (Exception e) {
            System.out.println("find course by invite code failed " + e);
            e.printStackTrace();
            return null;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 根据教师ID查找该教师创建的所有课程
     * @param teacherId 教师ID
     * @return 课程列表
     */
    public List<Course> getCoursesByTeacher(Long teacherId) {
        Session session = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            return dao.findCoursesByTeacherId(teacherId);
        } catch (Exception e) {
            System.out.println("get courses by teacher failed " + e);
            e.printStackTrace();
            return null;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 获取所有课程（供管理员使用）
     * @return 课程列表
     */
    public List<Course> getAllCourses() {
        Session session = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            return dao.findAllCourses();
        } catch (Exception e) {
            System.out.println("get all courses failed " + e);
            e.printStackTrace();
            return null;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 根据课程ID查找课程
     * @param courseId 课程ID
     * @return 课程信息，找不到返回null
     */
    public Course findCourseById(Long courseId) {
        Session session = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            return dao.findById(courseId);
        } catch (Exception e) {
            System.out.println("find course by id failed " + e);
            e.printStackTrace();
            return null;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 更新课程信息（仅限课程创建者或教务老师）
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param courseName 新的课程名称（可选）
     * @param description 新的课程描述（可选）
     * @return true表示更新成功，false表示更新失败
     */
    public boolean updateCourseById(Long courseId, Long teacherId, String courseName, String description) {
        Session session = null;
        Transaction tran = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            // 首先查找课程是否存在以及是否为该教师创建
            Course course = dao.findById(courseId);
            if (course == null) {
                System.out.println("课程不存在，ID: " + courseId);
                return false;
            }
            
            // 检查权限：教务老师可以更新所有课程，任课老师只能更新自己创建的课程
            boolean isAcademicAffairsTeacher = checkIfAcademicAffairsTeacher(teacherId);
            if (!isAcademicAffairsTeacher && !course.getTeacherId().equals(teacherId)) {
                System.out.println("权限不足，该课程不是由该教师创建，课程ID: " + courseId + ", 教师ID: " + teacherId);
                return false;
            }
            
            // 更新课程信息
            if (courseName != null && !courseName.trim().isEmpty()) {
                course.setCourseName(courseName.trim());
            }
            if (description != null) { // 允许description为空字符串
                course.setDescription(description);
            }
            
            tran = session.beginTransaction();
            dao.update(course);
            tran.commit();
            System.out.println("课程更新成功，ID: " + courseId);
            return true;
        } catch (Exception e) {
            System.out.println("更新课程失败 " + e);
            e.printStackTrace();
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 修改课程状态（开设中/结课）
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @param status 新的课程状态："0"=开设, "1"=结课
     * @return true表示更新成功，false表示更新失败
     */
    public boolean updateCourseStatus(Long courseId, Long teacherId, String status) {
        Session session = null;
        Transaction tran = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            // 首先查找课程是否存在以及是否为该教师创建
            Course course = dao.findById(courseId);
            if (course == null) {
                System.out.println("课程不存在，ID: " + courseId);
                return false;
            }
            
            // 检查权限：教务老师可以更新所有课程，任课老师只能更新自己创建的课程
            boolean isAcademicAffairsTeacher = checkIfAcademicAffairsTeacher(teacherId);
            if (!isAcademicAffairsTeacher && !course.getTeacherId().equals(teacherId)) {
                System.out.println("权限不足，该课程不是由该教师创建，课程ID: " + courseId + ", 教师ID: " + teacherId);
                return false;
            }
            
            // 验证状态值是否有效
            if (!"0".equals(status) && !"1".equals(status)) {
                System.out.println("无效的课程状态值: " + status + ", 课程ID: " + courseId);
                return false;
            }
            
            // 更新课程状态
            course.setStatus(status);
            
            tran = session.beginTransaction();
            dao.update(course);
            tran.commit();
            System.out.println("课程状态更新成功，ID: " + courseId + ", 新状态: " + status);
            return true;
        } catch (Exception e) {
            System.out.println("更新课程状态失败 " + e);
            e.printStackTrace();
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 检查教师是否为教务老师
     * @param teacherId 教师ID
     * @return true表示教务老师，false表示任课老师
     */
    private boolean checkIfAcademicAffairsTeacher(Long teacherId) {
        // 使用注入的TeacherService检查教师职位
        try {
            return teacherService.isAcademicAffairsTeacher(teacherId);
        } catch (Exception e) {
            System.out.println("检查教师职位失败: " + e.getMessage());
            e.printStackTrace();
            return false; // 默认认为不是教务老师
        }
    }
    
    /**
     * 删除教师自己创建的课程
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return true表示删除成功，false表示删除失败
     */
    public boolean deleteCourseById(Long courseId, Long teacherId) {
        Session session = null;
        Transaction tran = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            // 首先查找课程是否存在以及是否为该教师创建
            Course course = dao.findById(courseId);
            if (course == null) {
                System.out.println("课程不存在，ID: " + courseId);
                return false;
            }
            
            if (!course.getTeacherId().equals(teacherId)) {
                System.out.println("权限不足，该课程不是由该教师创建，课程ID: " + courseId + ", 教师ID: " + teacherId);
                return false;
            }
            
            tran = session.beginTransaction();
            // 删除课程
            dao.delete(course);
            tran.commit();
            System.out.println("课程删除成功，ID: " + courseId);
            return true;
        } catch (Exception e) {
            System.out.println("删除课程失败 " + e);
            e.printStackTrace();
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            // 不要在这里关闭session，让HibernateUtil管理session生命周期
        }
    }
    
    /**
     * 生成唯一的邀请码
     * @return 6位大写字母和数字的组合
     */
    private String generateInviteCode() {
        // 使用随机数生成6位邀请码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = (int) (Math.random() * 36);
            if (randomIndex < 26) {
                // 生成A-Z
                code.append((char) ('A' + randomIndex));
            } else {
                // 生成0-9
                code.append(randomIndex - 26);
            }
        }
        
        // 检查是否已存在
        while (findCourseByInviteCode(code.toString()) != null) {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                int randomIndex = (int) (Math.random() * 36);
                if (randomIndex < 26) {
                    code.append((char) ('A' + randomIndex));
                } else {
                    code.append(randomIndex - 26);
                }
            }
        }
        
        return code.toString();
    }
}