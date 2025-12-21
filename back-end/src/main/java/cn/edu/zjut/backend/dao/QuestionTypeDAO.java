package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.QuestionType;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class QuestionTypeDAO {

    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(QuestionTypeDAO.class);

    public List<QuestionType> queryAll() {
        String hql = "from QuestionType";
        try {
            Query<QuestionType> queryObject = session.createQuery(hql, QuestionType.class);
            return queryObject.list();
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }
}
