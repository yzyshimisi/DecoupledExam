package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamWrongBook;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ExamWrongBookDAO {

    private final Log log = LogFactory.getLog(ExamWrongBookDAO.class);
    @Setter
    private Session session;

    public void save(ExamWrongBook examWrongBook){
        log.debug("saving examWrongBook instance");
        try {
            session.save(examWrongBook);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }

    public void delete(ExamWrongBook examWrongBook){
        log.debug("deleting examWrongBook instance");
        try {
            session.delete(examWrongBook);
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        } finally{
        }
    }

    public void update(ExamWrongBook examWrongBook){
        log.debug("updating examWrongBook instance");
        try {
            session.save(examWrongBook);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        } finally{
        }
    }

    public ExamWrongBook queryById(Long id){
        String hql = "from ExamWrongBook where id = :id";
        try {
            Query<ExamWrongBook> queryObject = session.createQuery(hql, ExamWrongBook.class);
            queryObject.setParameter("id", id);
            return queryObject.list().get(0);
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }

    public List<ExamWrongBook> queryByStudentId(Long studentId){
        String hql = "from ExamWrongBook where studentId = :studentId";
        try {
            Query<ExamWrongBook> queryObject = session.createQuery(hql, ExamWrongBook.class);
            queryObject.setParameter("studentId", studentId);
            return queryObject.list();
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }
}
