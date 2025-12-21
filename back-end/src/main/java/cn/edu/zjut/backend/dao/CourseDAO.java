package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.Course;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class CourseDAO {
    private Session session;
    private final Log log = LogFactory.getLog(CourseDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加课程
     */
    public void add(Course course) {
        log.debug("saving course instance");
        try {
            session.save(course);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 根据邀请码查找课程
     */
    public Course findByInviteCode(String inviteCode) {
        try {
            String hql = "FROM Course WHERE inviteCode = :inviteCode";
            Query<Course> query = session.createQuery(hql, Course.class);
            query.setParameter("inviteCode", inviteCode);
            List<Course> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (RuntimeException re) {
            log.error("find course by invite code failed", re);
            throw re;
        }
    }

    /**
     * 根据课程ID查找课程
     */
    public Course findById(Long courseId) {
        try {
            return session.get(Course.class, courseId);
        } catch (RuntimeException re) {
            log.error("find course by id failed", re);
            throw re;
        }
    }

    /**
     * 查询教师创建的所有课程
     */
    public List<Course> findCoursesByTeacherId(Long teacherId) {
        try {
            String hql = "FROM Course WHERE teacherId = :teacherId";
            Query<Course> query = session.createQuery(hql, Course.class);
            query.setParameter("teacherId", teacherId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("query courses by teacher id failed", re);
            throw re;
        }
    }

    /**
     * 查询所有课程（供管理员使用）
     */
    public List<Course> findAllCourses() {
        try {
            String hql = "FROM Course";
            Query<Course> query = session.createQuery(hql, Course.class);
            return query.list();
        } catch (RuntimeException re) {
            log.error("query all courses failed", re);
            throw re;
        }
    }

    /**
     * 更新课程信息
     */
    public void update(Course course) {
        try {
            session.update(course);
        } catch (RuntimeException re) {
            log.error("update course failed", re);
            throw re;
        }
    }
}