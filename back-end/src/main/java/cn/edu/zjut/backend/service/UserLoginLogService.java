// UserLoginLogService.java
package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.UserLoginLogDAO;
import cn.edu.zjut.backend.po.UserLoginLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserLoginLogService implements cn.edu.zjut.backend.service.i.UserLoginLogService {
    private static final Logger logger = LoggerFactory.getLogger(UserLoginLogService.class);

    @Autowired
    private UserLoginLogDAO logDAO;

    @Override
    public void saveLog(UserLoginLog log) {
        Session session = HibernateUtil.getSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            logDAO.setSession(session);
            logDAO.save(log);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("保存用户登录日志失败", e);
        }
    }

    @Override
    public List<UserLoginLog> getLogList(int page, int size, String username, Integer loginStatus) {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.queryAll(page, size, username, loginStatus);
    }

    @Override
    public Long getTotalCount() {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.getTotalCount();
    }
}