// SecurityEventLogService.java
package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.SecurityEventLogDAO;
import cn.edu.zjut.backend.po.SecurityEventLog;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityEventLogService implements cn.edu.zjut.backend.service.i.SecurityEventLogService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventLogService.class);

    @Autowired
    private SecurityEventLogDAO logDAO;

    @Override
    public void saveLog(SecurityEventLog log) {
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
            logger.error("保存安全事件日志失败", e);
        }
    }

    @Override
    public List<SecurityEventLog> getLogList(int page, int size, String eventType, Integer riskLevel) {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.queryAll(page, size, eventType, riskLevel);
    }

    @Override
    public Long getTotalCount() {
        Session session = HibernateUtil.getSession();
        logDAO.setSession(session);
        return logDAO.getTotalCount();
    }
}