// SecurityEventLogDAO.java
package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SecurityEventLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventLogDAO.class);

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

    public void save(SecurityEventLog securityEventLog) {
        try {
            getSession().save(securityEventLog);
            logger.debug("安全事件日志保存成功");
        } catch (RuntimeException re) {
            logger.error("安全事件日志保存失败", re);
            throw re;
        }
    }

    @SuppressWarnings("unchecked")
    public List<SecurityEventLog> queryAll(int page, int size, String eventType, Integer riskLevel, Long userId, String ip, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("from SecurityEventLog where 1=1");
            
            // 添加查询条件
            if (eventType != null && !eventType.isEmpty()) {
                hql.append(" and eventType like :eventType");
            }
            if (riskLevel != null) {
                hql.append(" and riskLevel = :riskLevel");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (ip != null && !ip.isEmpty()) {
                hql.append(" and ipAddress like :ip");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and eventTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and eventTime <= :dateTo");
            }
            
            hql.append(" order by eventTime desc");
            
            org.hibernate.query.Query<SecurityEventLog> query = getSession().createQuery(hql.toString(), SecurityEventLog.class);
            
            // 设置参数
            if (eventType != null && !eventType.isEmpty()) {
                query.setParameter("eventType", "%" + eventType + "%");
            }
            if (riskLevel != null) {
                query.setParameter("riskLevel", riskLevel);
            }
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            if (ip != null && !ip.isEmpty()) {
                query.setParameter("ip", "%" + ip + "%");
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
            logger.error("安全事件日志查询失败", re);
            throw re;
        }
    }

    public Long getTotalCount(String eventType, Integer riskLevel, Long userId, String ip, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("select count(*) from SecurityEventLog where 1=1");
            
            // 添加查询条件
            if (eventType != null && !eventType.isEmpty()) {
                hql.append(" and eventType like :eventType");
            }
            if (riskLevel != null) {
                hql.append(" and riskLevel = :riskLevel");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (ip != null && !ip.isEmpty()) {
                hql.append(" and ipAddress like :ip");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and eventTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and eventTime <= :dateTo");
            }
            
            org.hibernate.query.Query<Long> query = getSession().createQuery(hql.toString(), Long.class);
            
            // 设置参数
            if (eventType != null && !eventType.isEmpty()) {
                query.setParameter("eventType", "%" + eventType + "%");
            }
            if (riskLevel != null) {
                query.setParameter("riskLevel", riskLevel);
            }
            if (userId != null) {
                query.setParameter("userId", userId);
            }
            if (ip != null && !ip.isEmpty()) {
                query.setParameter("ip", "%" + ip + "%");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                query.setParameter("dateFrom", java.sql.Timestamp.valueOf(dateFrom + " 00:00:00"));
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                query.setParameter("dateTo", java.sql.Timestamp.valueOf(dateTo + " 23:59:59"));
            }
            
            return query.uniqueResult();
        } catch (RuntimeException re) {
            logger.error("安全事件日志总数查询失败", re);
            throw re;
        }
    }
}