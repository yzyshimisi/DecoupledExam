package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.QuestionTags;
import lombok.Setter;
import org.apache.commons.logging.*;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class QuestionTagsDAO {
    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(QuestionTagsDAO.class);

    public void add(QuestionTags questionTags) {
        log.debug("saving questionTags instance");
        try {
            session.save(questionTags);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }

    public void delete(Long id) {
        log.debug("deleting questionTags instance");
        String hql = "delete from QuestionTags where id = :id";
        try {
            Query queryObject = session.createQuery(hql);
            queryObject.setParameter("id", id);
            queryObject.executeUpdate();
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        } finally{
        }
    }

    public void update(QuestionTags questionTags) {
        log.debug("updating questionTags instance");
        try {
            session.update(questionTags);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        } finally{
        }
    }

    public QuestionTags findById(Long id) {
        log.debug("querying questionTags instance");
        String hql = "from QuestionTags where id = :id";
        try {
            Query<QuestionTags> queryObject = session.createQuery(hql, QuestionTags.class);
            queryObject.setParameter("id", id);
            log.debug("query successful");
            return queryObject.list().get(0);
        } catch (RuntimeException re) {
            log.error("query failed", re);
            throw re;
        } finally{
        }
    }

    // 通过题目ID去查询
    public List<QuestionTags> query(Long questionId) {
        log.debug("querying questionTags instance");
        String hql = "from QuestionTags where questionId = :questionId";
        try {
            Query<QuestionTags> queryObject = session.createQuery(hql, QuestionTags.class);
            queryObject.setParameter("questionId", questionId);
            log.debug("query successful");
            return queryObject.list();
        } catch (RuntimeException re) {
            log.error("query failed", re);
            throw re;
        } finally{
        }
    }

    // 查询所有记录（用来导出成文件）
    public List<QuestionTags> queryAll() {
        log.debug("querying questionTags instance");
        String hql = "from QuestionTags";
        try {
            Query<QuestionTags> queryObject = session.createQuery(hql, QuestionTags.class);
            log.debug("query successful");
            return queryObject.list();
        } catch (RuntimeException re) {
            log.error("query failed", re);
            throw re;
        } finally{
        }
    }
}
