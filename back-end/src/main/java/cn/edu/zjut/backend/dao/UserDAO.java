package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class UserDAO {
    private Session session;
    private final Log log = LogFactory.getLog(UserDAO.class);

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加用户
     */
    public void add(User user) {
        log.debug("saving user instance");
        try {
            session.save(user);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 根据用户名查找用户（用于登录验证）
     */
    public User findByUsername(String username) {
        try {
            String hql = "from User where username = :username";
            Query<User> query = session.createQuery(hql, User.class);
            query.setParameter("username", username);
            List<User> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        } catch (RuntimeException re) {
            log.error("find user by username failed", re);
            throw re;
        }
    }

    /**
     * 根据用户ID查找用户
     */
    public User findById(Long userId) {
        try {
            return session.get(User.class, userId);
        } catch (RuntimeException re) {
            log.error("find user by id failed", re);
            throw re;
        }
    }

    /**
     * 查询所有用户或者根据类型查询用户
     */
    public List<User> query(Integer userType) {
        try {
            StringBuilder hql = new StringBuilder("from User where 1=1");
            if (userType != null) {
                hql.append(" and userType = :userType");
            }
            
            Query<User> query = session.createQuery(hql.toString(), User.class);
            if (userType != null) {
                query.setParameter("userType", userType);
            }
            
            return query.list();
        } catch (RuntimeException re) {
            log.error("query users failed", re);
            throw re;
        }
    }

    /**
     * 更新用户信息
     */
    public void update(User user) {
        try {
            session.update(user);
        } catch (RuntimeException re) {
            log.error("update user failed", re);
            throw re;
        }
    }

    /**
     * 更新用户状态
     */
    public void updateStatus(Long userId, String status) {
        try {
            String hql = "update User set status = :status where userId = :userId";
            Query query = session.createQuery(hql);
            query.setParameter("status", status);
            query.setParameter("userId", userId);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("update user status failed", re);
            throw re;
        }
    }

    /**
     * 删除用户
     */
    public void delete(Long userId) {
        try {
            String hql = "delete from User where userId = :userId";
            Query query = session.createQuery(hql);
            query.setParameter("userId", userId);
            query.executeUpdate();
        } catch (RuntimeException re) {
            log.error("delete user failed", re);
            throw re;
        }
    }
}