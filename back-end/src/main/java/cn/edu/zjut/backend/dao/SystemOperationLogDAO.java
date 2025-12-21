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
    public List<SystemOperationLog> queryAll(int page, int size, String module, String username) {
        try {
            StringBuilder hql = new StringBuilder("from SystemOperationLog order by logTime desc");
            // 可根据需要添加查询条件
            return getSession().createQuery(hql.toString())
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .list();
        } catch (RuntimeException re) {
            logger.error("系统操作日志查询失败", re);
            throw re;
        }
    }

    public Long getTotalCount() {
        try {
            return (Long) getSession().createQuery("select count(*) from SystemOperationLog")
                    .uniqueResult();
        } catch (RuntimeException re) {
            logger.error("系统操作日志总数查询失败", re);
            throw re;
        }
    }
}