package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamPaper;
import cn.edu.zjut.backend.po.ExamPaperQuestion;
import cn.edu.zjut.backend.po.ExamPaperQuestionId;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ExamPaperQuestionDAO {

    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(ExamPaperQuestionDAO.class);

    public void add(ExamPaperQuestion examPaperQuestion) {
        try {
            session.save(examPaperQuestion);
            log.debug("add successful");
        } catch (RuntimeException re) {
            log.error("add failed", re);
            throw re;
        } finally{
        }
    }

    public List<ExamPaperQuestion> query(Long paperId) {
        String hql = "from ExamPaperQuestion where id.paperId = :paperId";
        try {
            Query<ExamPaperQuestion> queryObject = session.createQuery(hql, ExamPaperQuestion.class);
            queryObject.setParameter("paperId", paperId);
            return queryObject.list();
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }

    public ExamPaperQuestion query(ExamPaperQuestionId examPaperQuestionId) {
        try {
            ExamPaperQuestion examPaperQuestion = session.get(ExamPaperQuestion.class, examPaperQuestionId);
            return examPaperQuestion;
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }

    public void delete(ExamPaperQuestion examPaperQuestion) {
        try {
            session.delete(examPaperQuestion);
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        } finally{
        }
    }

    public void update(ExamPaperQuestion examPaperQuestion) {
        try {
            session.update(examPaperQuestion);
            log.debug("add successful");
        } catch (RuntimeException re) {
            log.error("add failed", re);
            throw re;
        } finally{
        }
    }
}
