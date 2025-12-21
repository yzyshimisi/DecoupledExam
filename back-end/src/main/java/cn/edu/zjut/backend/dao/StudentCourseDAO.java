package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.StudentCourse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

public class StudentCourseDAO {
    private Session session;
    private final Log log = LogFactory.getLog(StudentCourseDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 学生加入课程
     */
    public void add(StudentCourse studentCourse) {
        log.debug("saving student course instance");
        try {
            session.save(studentCourse);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 根据学生ID和课程ID查找选课记录
     */
    public StudentCourse findByStudentIdAndCourseId(Long studentId, Long courseId) {
        try {
            String hql = "from StudentCourse where studentId = :studentId and courseId = :courseId";
            Query<StudentCourse> query = session.createQuery(hql, StudentCourse.class);
            query.setParameter("studentId", studentId);
            query.setParameter("courseId", courseId);
            List<StudentCourse> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (RuntimeException re) {
            log.error("find student course by studentId and courseId failed", re);
            throw re;
        }
    }

    /**
     * 根据学生ID查找所有选课记录
     */
    public List<StudentCourse> findByStudentId(Long studentId) {
        try {
            String hql = "from StudentCourse where studentId = :studentId";
            Query<StudentCourse> query = session.createQuery(hql, StudentCourse.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("query student courses by student id failed", re);
            throw re;
        }
    }

    /**
     * 根据课程ID查找所有选课记录
     */
    public List<StudentCourse> findByCourseId(Long courseId) {
        try {
            String hql = "from StudentCourse where courseId = :courseId";
            Query<StudentCourse> query = session.createQuery(hql, StudentCourse.class);
            query.setParameter("courseId", courseId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("query student courses by course id failed", re);
            throw re;
        }
    }

    /**
     * 更新选课信息
     */
    public void update(StudentCourse studentCourse) {
        try {
            session.update(studentCourse);
        } catch (RuntimeException re) {
            log.error("update student course failed", re);
            throw re;
        }
    }

    /**
     * 删除选课记录（退课）
     */
    public void delete(StudentCourse studentCourse) {
        try {
            session.delete(studentCourse);
        } catch (RuntimeException re) {
            log.error("delete student course failed", re);
            throw re;
        }
    }
}