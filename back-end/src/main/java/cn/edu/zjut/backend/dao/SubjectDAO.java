package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.Subject;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class SubjectDAO {

    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(SubjectDAO.class);

    public void add(Subject subject) throws HibernateException {
        try {
            session.save(subject);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }

    public List<Subject> query(int id) throws HibernateException {
        try {
            if(id==-1){     // 查询全部
                String hql = "from Subject";
                Query<Subject> queryObject = session.createQuery(hql, Subject.class);
                return queryObject.list();
            }else{
                String hql = "from Subject where subjectId=:id";
                Query<Subject> queryObject = session.createQuery(hql, Subject.class);
                queryObject.setParameter("id", id);
                return queryObject.list();
            }
        } catch (RuntimeException re) {
            log.error("获取数据失败", re);
            throw re;
        } finally{
        }
    }

    public Subject query(String name) throws HibernateException {
        try {
            String hql = "from Subject where subjectName=:name";
            Query<Subject> queryObject = session.createQuery(hql, Subject.class);
            queryObject.setParameter("name", name);
            return queryObject.list().get(0);
        } catch (RuntimeException re) {
            log.error("获取数据失败", re);
            throw re;
        } finally{
        }
    }

    public void delete(int id) throws HibernateException {
        try {
            String hql = "delete from Subject where subjectId=:id";
            Query queryObject = session.createQuery(hql);
            queryObject.setParameter("id", id);
            queryObject.executeUpdate();
        } catch (RuntimeException re) {
            log.error("删除失败", re);
            throw re;
        } finally{
        }
    }
}
