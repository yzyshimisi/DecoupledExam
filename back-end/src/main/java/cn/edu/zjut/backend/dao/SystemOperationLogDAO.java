// SystemOperationLogDAO.java
package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.SystemOperationLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SystemOperationLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(SystemOperationLogDAO.class);

    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        if (session == null) {
            return HibernateUtil.getSession();
        }
        return session;
    }

    public void save(SystemOperationLog systemOperationLog) {
        try {
            getSession().save(systemOperationLog);
            logger.debug("系统操作日志保存成功");
        } catch (RuntimeException re) {
            logger.error("系统操作日志保存失败", re);
            throw re;
        }
    }

    @SuppressWarnings("unchecked")
    public List<SystemOperationLog> queryAll(int page, int size, String module, String username, Long userId, String action, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("from SystemOperationLog where 1=1");
            
            // 添加查询条件
            if (module != null && !module.isEmpty()) {
                hql.append(" and module like :module");
            }
            if (username != null && !username.isEmpty()) {
                hql.append(" and userId is not null");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (action != null && !action.isEmpty()) {
                hql.append(" and action like :action");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and logTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and logTime <= :dateTo");
            }
            
            hql.append(" order by logTime desc");
            
            org.hibernate.query.Query<SystemOperationLog> query = getSession().createQuery(hql.toString(), SystemOperationLog.class);
            
            // 设置参数
            if (module != null && !module.isEmpty()) {
                query.setParameter("module", "%" + module + "%");
            }
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            if (action != null && !action.isEmpty()) {
                query.setParameter("action", "%" + action + "%");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                query.setParameter("dateFrom", java.sql.Timestamp.valueOf(dateFrom + " 00:00:00"));
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                query.setParameter("dateTo", java.sql.Timestamp.valueOf(dateTo + " 23:59:59"));
            }
            
            // 设置分页参数
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            
            return query.list();
        } catch (RuntimeException re) {
            logger.error("系统操作日志查询失败", re);
            throw re;
        }
    }

    public Long getTotalCount(String module, String username, Long userId, String action, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("select count(*) from SystemOperationLog where 1=1");
            
            // 添加查询条件
            if (module != null && !module.isEmpty()) {
                hql.append(" and module like :module");
            }
            if (username != null && !username.isEmpty()) {
                hql.append(" and userId is not null");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (action != null && !action.isEmpty()) {
                hql.append(" and action like :action");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and logTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and logTime <= :dateTo");
            }
            
            org.hibernate.query.Query<Long> query = getSession().createQuery(hql.toString(), Long.class);
            
            // 设置参数
            if (module != null && !module.isEmpty()) {
                query.setParameter("module", "%" + module + "%");
            }
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            if (action != null && !action.isEmpty()) {
                query.setParameter("action", "%" + action + "%");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                query.setParameter("dateFrom", java.sql.Timestamp.valueOf(dateFrom + " 00:00:00"));
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                query.setParameter("dateTo", java.sql.Timestamp.valueOf(dateTo + " 23:59:59"));
            }
            
            return query.uniqueResult();
        } catch (RuntimeException re) {
            logger.error("系统操作日志总数查询失败", re);
            throw re;
        }
    }
}