package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.CourseDAO;
import cn.edu.zjut.backend.po.Course;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("courseServ")
public class CourseService {
    
    public Session getSession() {
        return HibernateUtil.getSession();
    }
    
    /**
     * 创建课程
     * @param course 课程信息
     * @return 是否创建成功
     */
    public boolean createCourse(Course course) {
        Session session = null;
        Transaction tran = null;
        try {
            session = getSession();
            CourseDAO dao = new CourseDAO();
            dao.setSession(session);
            
            // 生成邀请码
            String inviteCode = generateInviteCode();
            course.setInviteCode(inviteCode);
            
            tran = session.beginTransaction();
            dao.add(course);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("create course failed " + e);
            e.printStackTrace(); // 打印详细异常信息
            if (tran != null) {
                tran.rollback();
            }
            return false;
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