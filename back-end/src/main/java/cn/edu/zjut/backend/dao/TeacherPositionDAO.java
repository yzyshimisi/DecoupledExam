package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.TeacherPosition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class TeacherPositionDAO {
    private Session session;
    private final Log log = LogFactory.getLog(TeacherPositionDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加教师职位记录
     */
    public void add(TeacherPosition teacherPosition) {
        log.debug("saving TeacherPosition instance");
        try {
            session.save(teacherPosition);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 根据教师ID查找职位记录
     */
    public TeacherPosition findByTeacherId(Long teacherId) {
        try {
            String hql = "from TeacherPosition where teacherId = :teacherId";
            Query<TeacherPosition> query = session.createQuery(hql, TeacherPosition.class);
            query.setParameter("teacherId", teacherId);
            List<TeacherPosition> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (RuntimeException re) {
            log.error("find TeacherPosition by teacherId failed", re);
            throw re;
        }
    }

    /**
     * 根据ID查找职位记录
     */
    public TeacherPosition findById(Long id) {
        try {
            return session.get(TeacherPosition.class, id);
        } catch (RuntimeException re) {
            log.error("find TeacherPosition by id failed", re);
            throw re;
        }
    }

    /**
     * 根据角色查询教师
     */
    public List<TeacherPosition> findByRole(Byte role) {
        try {
            String hql = "from TeacherPosition where role = :role";
            Query<TeacherPosition> query = session.createQuery(hql, TeacherPosition.class);
            query.setParameter("role", role);
            return query.list();
        } catch (RuntimeException re) {
            log.error("find TeacherPositions by role failed", re);
            throw re;
        }
    }

    /**
     * 更新教师职位信息
     */
    public void update(TeacherPosition teacherPosition) {
        try {
            session.update(teacherPosition);
        } catch (RuntimeException re) {
            log.error("update TeacherPosition failed", re);
            throw re;
        }
    }

    /**
     * 删除教师职位记录
     */
    public void delete(Long id) {
        try {
            String hql = "delete from TeacherPosition where id = :id";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("delete TeacherPosition failed", re);
            throw re;
        }
    }
}