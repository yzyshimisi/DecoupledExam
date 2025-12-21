package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.TeacherSubject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TeacherSubjectDAO {
    private Session session;
    private final Log log = LogFactory.getLog(TeacherSubjectDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加教师学科关联
     */
    public void add(TeacherSubject teacherSubject) {
        log.debug("saving TeacherSubject instance");
        try {
            session.save(teacherSubject);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 根据教师ID查找所有关联学科
     */
    public List<TeacherSubject> findByTeacherId(Long teacherId) {
        try {
            String hql = "from TeacherSubject where teacherId = :teacherId";
            Query<TeacherSubject> query = session.createQuery(hql, TeacherSubject.class);
            query.setParameter("teacherId", teacherId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("find TeacherSubjects by teacherId failed", re);
            throw re;
        }
    }

    /**
     * 根据学科ID查找所有关联教师
     */
    public List<TeacherSubject> findBySubjectId(Integer subjectId) {
        try {
            String hql = "from TeacherSubject where subjectId = :subjectId";
            Query<TeacherSubject> query = session.createQuery(hql, TeacherSubject.class);
            query.setParameter("subjectId", subjectId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("find TeacherSubjects by subjectId failed", re);
            throw re;
        }
    }

    /**
     * 根据教师ID和学科ID查找关联记录
     */
    public TeacherSubject findByTeacherIdAndSubjectId(Long teacherId, Integer subjectId) {
        try {
            String hql = "from TeacherSubject where teacherId = :teacherId and subjectId = :subjectId";
            Query<TeacherSubject> query = session.createQuery(hql, TeacherSubject.class);
            query.setParameter("teacherId", teacherId);
            query.setParameter("subjectId", subjectId);
            List<TeacherSubject> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (RuntimeException re) {
            log.error("find TeacherSubject by teacherId and subjectId failed", re);
            throw re;
        }
    }

    /**
     * 查找主教学科的教师
     */
    public List<TeacherSubject> findMainTeachers() {
        try {
            String hql = "from TeacherSubject where isMain = 1";
            Query<TeacherSubject> query = session.createQuery(hql, TeacherSubject.class);
            return query.list();
        } catch (RuntimeException re) {
            log.error("find main teachers failed", re);
            throw re;
        }
    }

    /**
     * 更新教师学科关联信息
     */
    public void update(TeacherSubject teacherSubject) {
        try {
            session.update(teacherSubject);
        } catch (RuntimeException re) {
            log.error("update TeacherSubject failed", re);
            throw re;
        }
    }

    /**
     * 删除教师学科关联记录
     */
    public void deleteByTeacherIdAndSubjectId(Long teacherId, Integer subjectId) {
        try {
            String hql = "delete from TeacherSubject where teacherId = :teacherId and subjectId = :subjectId";
            Query query = session.createQuery(hql);
            query.setParameter("teacherId", teacherId);
            query.setParameter("subjectId", subjectId);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("delete TeacherSubject failed", re);
            throw re;
        }
    }

    /**
     * 删除某教师的所有学科关联
     */
    public void deleteByTeacherId(Long teacherId) {
        try {
            String hql = "delete from TeacherSubject where teacherId = :teacherId";
            Query query = session.createQuery(hql);
            query.setParameter("teacherId", teacherId);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("delete TeacherSubject by teacherId failed", re);
            throw re;
        }
    }

    /**
     * 删除某学科的所有教师关联
     */
    public void deleteBySubjectId(Integer subjectId) {
        try {
            String hql = "delete from TeacherSubject where subjectId = :subjectId";
            Query query = session.createQuery(hql);
            query.setParameter("subjectId", subjectId);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("delete TeacherSubject by subjectId failed", re);
            throw re;
        }
    }
}