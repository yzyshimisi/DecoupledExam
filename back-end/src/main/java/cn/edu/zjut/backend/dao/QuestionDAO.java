package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.util.UserContext;
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

    public List<Questions> query(QuestionQueryDTO filterDTO){

        try {
            StringBuilder hql = new StringBuilder("FROM Questions q WHERE 1=1");
            StringBuilder conditions = new StringBuilder();

            // 1. 仅显示我的题目
            if (filterDTO.isMineOnly()) {
                conditions.append(" AND q.creatorId = :creatorId");
            }

            // 2. 题干关键词
            if (filterDTO.getStemKeyword() != null && !filterDTO.getStemKeyword().isEmpty()) {
                conditions.append(" AND q.title LIKE :stemKeyword");
            }

            // 3. 选择的题型
            if (filterDTO.getSelectedTypes() != null && !filterDTO.getSelectedTypes().isEmpty()) {
                conditions.append(" AND q.typeId IN :selectedTypes");
            }

            // 4. 选择的学科
            if (filterDTO.getSelectedSubjects() != null && !filterDTO.getSelectedSubjects().isEmpty()) {
                conditions.append(" AND q.subjectId IN :selectedSubjects");
            }

            // 5. 最大难度
            if (filterDTO.getMaxDifficulty()!=null && filterDTO.getMaxDifficulty()>0) {
                conditions.append(" AND q.difficulty <= :maxDifficulty");
            }

            // 6. 选择的审核状态
            if (filterDTO.getSelectedStatuses() != null && !filterDTO.getSelectedStatuses().isEmpty()) {
                conditions.append(" AND q.reviewStatus IN :selectedStatuses");
            }

            // 7. 出题人查询
            if (filterDTO.getAuthorQuery() != null) {
                conditions.append(" AND q.creatorId = :authorId");
            }

            hql.append(conditions);
            System.out.println(hql.toString());

            // 创建查询
            Query<Questions> query = session.createQuery(hql.toString(), Questions.class);

            // 设置参数
            if (filterDTO.isMineOnly()) {
                query.setParameter("creatorId", UserContext.getUserId()); // 需要获取当前用户ID
                System.out.println(UserContext.getUserId());
            }

            if (filterDTO.getStemKeyword() != null && !filterDTO.getStemKeyword().isEmpty()) {
                query.setParameter("stemKeyword", "%" + filterDTO.getStemKeyword() + "%");
            }

            if (filterDTO.getSelectedTypes() != null && !filterDTO.getSelectedTypes().isEmpty()) {
                query.setParameter("selectedTypes", filterDTO.getSelectedTypes());
            }

            if (filterDTO.getSelectedSubjects() != null && !filterDTO.getSelectedSubjects().isEmpty()) {
                query.setParameter("selectedSubjects", filterDTO.getSelectedSubjects());
            }

            if (filterDTO.getMaxDifficulty() !=null && filterDTO.getMaxDifficulty() > 0) {
                query.setParameter("maxDifficulty", filterDTO.getMaxDifficulty().byteValue());
            }

            if (filterDTO.getSelectedStatuses() != null && !filterDTO.getSelectedStatuses().isEmpty()) {
                query.setParameter("selectedStatuses", filterDTO.getSelectedStatuses());
            }

            if (filterDTO.getAuthorQuery() != null) {
                query.setParameter("authorId", filterDTO.getAuthorQuery());
            }

            return query.list();
        } catch (RuntimeException re) {
            log.error("query failed", re);
            return null;
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
