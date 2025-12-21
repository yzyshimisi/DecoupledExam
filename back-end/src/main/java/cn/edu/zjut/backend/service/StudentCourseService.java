package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.CourseDAO;
import cn.edu.zjut.backend.dao.StudentCourseDAO;
import cn.edu.zjut.backend.po.Course;
import cn.edu.zjut.backend.po.StudentCourse;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("studentCourseServ")
public class StudentCourseService {
    
    public Session getSession() {
        return HibernateUtil.getSession();
    }
    
    /**
     * 学生通过邀请码加入课程
     * @param studentId 学生ID
     * @param inviteCode 课程邀请码
     * @return 是否加入成功
     */
    public boolean joinCourseByInviteCode(Long studentId, String inviteCode) {
        Session session = null;
        try {
            System.out.println("=== 开始处理学生加入课程请求 ===");
            System.out.println("学生ID: " + studentId);
            System.out.println("邀请码: " + inviteCode);
            
            session = getSession();
            CourseDAO courseDao = new CourseDAO();
            StudentCourseDAO studentCourseDao = new StudentCourseDAO();
            courseDao.setSession(session);
            studentCourseDao.setSession(session);
            
            Transaction transaction = session.beginTransaction();
            
            // 查找课程
            System.out.println("正在查找课程...");
            Course course = courseDao.findByInviteCode(inviteCode);
            if (course == null) {
                System.out.println("未找到对应邀请码的课程");
                return false; // 课程不存在
            }
            
            System.out.println("找到课程，课程ID: " + course.getCourseId() + "，课程名称: " + course.getCourseName());
            
            // 检查学生是否已经加入了这门课程
            System.out.println("正在检查学生是否已加入该课程...");
            StudentCourse existingRecord = studentCourseDao.findByStudentIdAndCourseId(studentId, course.getCourseId());
            if (existingRecord != null) {
                System.out.println("学生已加入该课程，选课记录ID: " + existingRecord.getId());
                return false; // 学生已经加入了这门课程
            }
            
            // 创建选课记录
            System.out.println("创建新的选课记录...");
            StudentCourse studentCourse = new StudentCourse();
            studentCourse.setStudentId(studentId);
            studentCourse.setCourseId(course.getCourseId());
            studentCourse.setJoinTime(new Date());
            studentCourse.setStatus("0"); // 正常学习状态
            
            studentCourseDao.add(studentCourse);
            transaction.commit();
            System.out.println("成功创建选课记录，选课记录ID: " + studentCourse.getId());
            return true;
        } catch (Exception e) {
            System.out.println("join course by invite code failed " + e.getMessage());
            e.printStackTrace();
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            return false;
        } finally {
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }
    
    /**
     * 学生退出课程
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否退出成功
     */
    public boolean quitCourse(Long studentId, Long courseId) {
        Session session = null;
        try {
            session = getSession();
            StudentCourseDAO studentCourseDao = new StudentCourseDAO();
            studentCourseDao.setSession(session);
            
            Transaction transaction = session.beginTransaction();
            
            // 查找选课记录
            StudentCourse studentCourse = studentCourseDao.findByStudentIdAndCourseId(studentId, courseId);
            if (studentCourse == null) {
                return false; // 未找到选课记录
            }
            
            // 更新状态为已退课
            studentCourse.setStatus("1");
            studentCourseDao.update(studentCourse);
            transaction.commit();
            return true;
        } catch (Exception e) {
            System.out.println("quit course failed " + e);
            if (session != null && session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            return false;
        } finally {
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }
    
    /**
     * 获取学生加入的所有课程
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    public List<StudentCourse> getStudentCourses(Long studentId) {
        Session session = getSession();
        StudentCourseDAO dao = new StudentCourseDAO();
        dao.setSession(session);
        
        try {
            return dao.findByStudentId(studentId);
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * 获取课程的所有学生
     * @param courseId 课程ID
     * @return 选课记录列表
     */
    public List<StudentCourse> getCourseStudents(Long courseId) {
        Session session = getSession();
        StudentCourseDAO dao = new StudentCourseDAO();
        dao.setSession(session);
        
        try {
            return dao.findByCourseId(courseId);
        } finally {
            HibernateUtil.closeSession();
        }
    }
}