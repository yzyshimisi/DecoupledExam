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
    public List<SecurityEventLog> queryAll(int page, int size, String eventType, Integer riskLevel) {
        try {
            StringBuilder hql = new StringBuilder("from SecurityEventLog order by eventTime desc");
            // 可根据需要添加查询条件
            return getSession().createQuery(hql.toString())
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .list();
        } catch (RuntimeException re) {
            logger.error("安全事件日志查询失败", re);
            throw re;
        }
    }

    public Long getTotalCount() {
        try {
            return (Long) getSession().createQuery("select count(*) from SecurityEventLog")
                    .uniqueResult();
        } catch (RuntimeException re) {
            logger.error("安全事件日志总数查询失败", re);
            throw re;
        }
    }
}