// ExamDAO.java
package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.Exam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExamDAO {
    private Session session;
    private final Log log = LogFactory.getLog(ExamDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    // 新增考试
    public void addExam(Exam exam) {
        log.debug("saving exam instance");
        try {
            session.save(exam);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    // 按状态和教师ID查询考试
    public List<Exam> queryByStatus(Byte status, Long teacherId) {
        try {
            StringBuilder hql = new StringBuilder("from Exam e where 1=1");

            if (status != null) {
                hql.append(" AND e.status = :status");
            }
            if (teacherId != null) {
                hql.append(" AND e.teacherId = :teacherId");
            }

            Query<Exam> query = session.createQuery(hql.toString(), Exam.class);

            if (status != null) {
                query.setParameter("status", status);
            }
            if (teacherId != null) {
                query.setParameter("teacherId", teacherId);
            }

            return query.list();
        } catch (RuntimeException re) {
            log.error("query failed", re);
            throw re;
        }
    }

    // 根据ID查询考试
    public Exam getById(Long examId) {
        try {
            return session.get(Exam.class, examId);
        } catch (RuntimeException re) {
            log.error("get exam failed", re);
            throw re;
        }
    }

    // 更新考试信息
    public void updateExam(Exam exam) {
        log.debug("updating exam instance");
        try {
            session.update(exam);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        }
    }

    // 删除考试
    public void deleteExam(Long examId) {
        log.debug("deleting exam instance");
        try {
            Exam exam = session.get(Exam.class, examId);
            if (exam != null) {
                session.delete(exam);
            }
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }
}