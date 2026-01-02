package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.dto.SubjectQueryDTO;
import cn.edu.zjut.backend.po.Subject;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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

    public List<Subject> query(SubjectQueryDTO filterDTO) throws HibernateException {
        try {
            StringBuilder hql = new StringBuilder("from Subject where 1=1");

            if(filterDTO==null){
                Query<Subject> queryObject = session.createQuery(hql.toString(), Subject.class);
                return queryObject.list();
            }

            // 动态拼接查询条件
            if (filterDTO.getSubjectId() != null) {
                hql.append(" and subjectId = :subjectId");
            }
            if (StringUtils.isNotBlank(filterDTO.getSubjectName())) { // 建议使用 StringUtils 工具类判空
                hql.append(" and subjectName like :subjectName");
            }
            if (StringUtils.isNotBlank(filterDTO.getSubjectCode())) {
                hql.append(" and subjectCode like :subjectCode");
            }
            if (filterDTO.getGradeLevels() != null) {
                hql.append(" and gradeLevel in :gradeLevels");
            }
            if (filterDTO.getStatus() != null) {
                hql.append(" and status = :status");
            }

            Query<Subject> queryObject = session.createQuery(hql.toString(), Subject.class);

            // 动态设置参数
            if (filterDTO.getSubjectId() != null) {
                queryObject.setParameter("subjectId", filterDTO.getSubjectId());
            }
            if (StringUtils.isNotBlank(filterDTO.getSubjectName())) {
                queryObject.setParameter("subjectName", "%" + filterDTO.getSubjectName() + "%"); // 模糊查询
            }
            if (StringUtils.isNotBlank(filterDTO.getSubjectCode())) {
                queryObject.setParameter("subjectCode", "%" + filterDTO.getSubjectCode() + "%"); // 模糊查询
            }
            if (filterDTO.getGradeLevels() != null) {
                queryObject.setParameter("gradeLevels", filterDTO.getGradeLevels());
            }
            if (filterDTO.getStatus() != null) {
                queryObject.setParameter("status", filterDTO.getStatus());
            }

            return queryObject.list();

        } catch (RuntimeException re) {
            log.error("获取数据失败", re);
            return null;
        } finally {
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
