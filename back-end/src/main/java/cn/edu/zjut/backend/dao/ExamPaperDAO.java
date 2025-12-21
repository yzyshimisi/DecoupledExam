package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamPaper;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ExamPaperDAO {
    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(ExamPaperDAO.class);

    public void add(ExamPaper examPaper) {
        try {
            session.save(examPaper);
            log.debug("add successful");
        } catch (RuntimeException re) {
            log.error("add failed", re);
            throw re;
        } finally{
        }
    }

    public List<ExamPaper> query(Long creatorId) {
        String hql = "from ExamPaper where creatorId = :creatorId";
        try {
            Query<ExamPaper> queryObject = session.createQuery(hql, ExamPaper.class);
            queryObject.setParameter("creatorId", creatorId);
            return queryObject.list();
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }

    public void delete(Long paperId) {
        String hql = "delete from ExamPaper where paperId = :paperId";
        try {
            Query queryObject = session.createQuery(hql);
            queryObject.setParameter("paperId", paperId);
            queryObject.executeUpdate();
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        } finally{
        }
    }

    public void update(ExamPaper examPaper) {
        try {
            session.update(examPaper);
            log.debug("add successful");
        } catch (RuntimeException re) {
            log.error("add failed", re);
            throw re;
        } finally{
        }
    }
}
