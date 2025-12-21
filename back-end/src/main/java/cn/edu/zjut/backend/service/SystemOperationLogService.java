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
            logger.error("保存系统操作日志失败", e);
        }
    }

    @Override
    public List<SystemOperationLog> getLogList(int page, int size, String module, String username) {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.queryAll(page, size, module, username);
    }

    @Override
    public Long getTotalCount() {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.getTotalCount();
    }
}