package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.SystemOperationLogDAO;
import cn.edu.zjut.backend.po.SystemOperationLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemOperationLogService implements cn.edu.zjut.backend.service.i.SystemOperationLogService {
    private static final Logger logger = LoggerFactory.getLogger(SystemOperationLogService.class);
    
    @Autowired
    private SystemOperationLogDAO logDAO;

    @Override
    public void saveLog(SystemOperationLog log) {
        Session session = null;
        Transaction transaction = null;

        try {
            // 使用独立的Session，避免与主线程事务冲突
            // 在AOP的finally块中，主线程事务可能已经结束
            session = HibernateUtil.getNewSession();
            transaction = session.beginTransaction();
            logDAO.setSession(session);
            logDAO.save(log);
            transaction.commit();
            logger.debug("系统操作日志保存成功，用户ID: {}, 操作: {}", log.getUserId(), log.getAction());
        } catch (Exception e) {
            logger.error("保存系统操作日志失败: {}", e.getMessage(), e);
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    logger.error("回滚事务失败: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            // 不抛出异常，避免影响主业务流程
        } finally {
            // 确保session被正确关闭
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (Exception e) {
                    logger.error("关闭Hibernate会话失败", e);
                }
            }
        }
    }

    @Override
    public List<SystemOperationLog> getLogList(int page, int size, String module, String username) {
        return getLogList(page, size, module, username, null, null, null, null);
    }
    
    @Override
    public List<SystemOperationLog> getLogList(int page, int size, String module, String username, Long userId, String action, String dateFrom, String dateTo) {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        try {
            return logDAO.queryAll(page, size, module, username, userId, action, dateFrom, dateTo);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    @Override
    public Long getTotalCount(String module, String username) {
        return getTotalCount(module, username, null, null, null, null);
    }
    
    @Override
    public Long getTotalCount(String module, String username, Long userId, String action, String dateFrom, String dateTo) {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        try {
            return logDAO.getTotalCount(module, username, userId, action, dateFrom, dateTo);
        } finally {
            HibernateUtil.closeSession();
        }
    }
    
    @Override
    public Long getTotalCount() {
        return getTotalCount(null, null, null, null, null, null);
    }
}