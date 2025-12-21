package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class QuestionDAO {

    @Setter
    private Session session;
    private final Log log = LogFactory.getLog(QuestionDAO.class);

    public void add(Questions question) {
        log.debug("saving question instance");
        try {
            session.save(question);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }

    public Questions query(Long id) {
        String hql = "from Questions where id = :id";
        try {
            Query<Questions> queryObject = session.createQuery(hql, Questions.class);
            queryObject.setParameter("id", id);
            return queryObject.list().get(0);
        } catch (RuntimeException re) {
            System.out.println("find by hql failed"+re);
            throw re;
        } finally{
        }
    }

    public List<Questions> query(QuestionQueryDTO dto){

        try {
            StringBuilder hql = new StringBuilder("from Questions q where 1=1");

            // 所有条件判断统一对齐，使用dto.xxx获取参数
            if (dto.getTypeId() != null) {
                hql.append(" AND q.typeId = :typeId");
            }
            if (dto.getDifficulty() != null) {
                hql.append(" AND q.difficulty = :difficulty");
            }
            if (dto.getSubjectId() != null) {
                hql.append(" AND q.subjectId = :subjectId");
            }
            if (dto.getCreatorId() != null) {
                hql.append(" AND q.creatorId = :creatorId");
            }
            if (dto.getReviewStatus() != null) {
                hql.append(" AND q.reviewStatus = :reviewStatus");
            }

            System.out.println(hql.toString());
            Query<Questions> query = session.createQuery(hql.toString(), Questions.class);

            // 参数绑定
            if (dto.getTypeId() != null) {
                query.setParameter("typeId", dto.getTypeId());
            }
            if (dto.getDifficulty() != null) {
                query.setParameter("difficulty", dto.getDifficulty());
            }
            if (dto.getSubjectId() != null) {
                query.setParameter("subjectId", dto.getSubjectId());
            }
            if (dto.getCreatorId() != null) {
                query.setParameter("creatorId", dto.getCreatorId());
            }
            if (dto.getReviewStatus() != null) {
                query.setParameter("reviewStatus", dto.getReviewStatus());
            }

            return query.list();
        } catch (RuntimeException re) {
            log.error("query failed", re);
            throw re;
        } finally{
        }
    }

    public void delete(Questions questions) {
        try {
            session.delete(questions);
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        } finally{
        }
    }

    public void update(Questions question) {
        try {
            session.update(question);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        } finally{
        }
    }
}
