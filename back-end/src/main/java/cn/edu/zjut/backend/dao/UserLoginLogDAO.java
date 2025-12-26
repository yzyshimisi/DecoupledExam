// UserLoginLogDAO.java
package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.UserLoginLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class UserLoginLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserLoginLogDAO.class);

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

    public void save(UserLoginLog userLoginLog) {
        try {
            getSession().save(userLoginLog);
            logger.debug("用户登录日志保存成功");
        } catch (RuntimeException re) {
            logger.error("用户登录日志保存失败", re);
            throw re;
        }
    }

    @SuppressWarnings("unchecked")
    public List<UserLoginLog> queryAll(int page, int size, String username, Integer loginStatus, Long userId, String ip, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("from UserLoginLog where 1=1");
            
            // 添加查询条件
            if (username != null && !username.isEmpty()) {
                hql.append(" and username like :username");
            }
            if (loginStatus != null) {
                hql.append(" and loginStatus = :loginStatus");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (ip != null && !ip.isEmpty()) {
                hql.append(" and ipAddress like :ip");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and loginTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and loginTime <= :dateTo");
            }
            
            hql.append(" order by loginTime desc");
            
            org.hibernate.query.Query<UserLoginLog> query = getSession().createQuery(hql.toString(), UserLoginLog.class);
            
            // 设置参数
            if (username != null && !username.isEmpty()) {
                query.setParameter("username", "%" + username + "%");
            }
            if (loginStatus != null) {
                query.setParameter("loginStatus", loginStatus);
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
            logger.error("用户登录日志查询失败", re);
            throw re;
        }
    }

    public Long getTotalCount(String username, Integer loginStatus, Long userId, String ip, String dateFrom, String dateTo) {
        try {
            StringBuilder hql = new StringBuilder("select count(*) from UserLoginLog where 1=1");
            
            // 添加查询条件
            if (username != null && !username.isEmpty()) {
                hql.append(" and username like :username");
            }
            if (loginStatus != null) {
                hql.append(" and loginStatus = :loginStatus");
            }
            if (userId != null) {
                hql.append(" and userId = :userId");
            }
            if (ip != null && !ip.isEmpty()) {
                hql.append(" and ipAddress like :ip");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                hql.append(" and loginTime >= :dateFrom");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                hql.append(" and loginTime <= :dateTo");
            }
            
            org.hibernate.query.Query<Long> query = getSession().createQuery(hql.toString(), Long.class);
            
            // 设置参数
            if (username != null && !username.isEmpty()) {
                query.setParameter("username", "%" + username + "%");
            }
            if (loginStatus != null) {
                query.setParameter("loginStatus", loginStatus);
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
            logger.error("用户登录日志总数查询失败", re);
            throw re;
        }
    }
}