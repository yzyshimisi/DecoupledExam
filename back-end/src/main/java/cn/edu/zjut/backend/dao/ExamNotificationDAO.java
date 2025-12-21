package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamNotification;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class ExamNotificationDAO {
    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    public void save(ExamNotification notification) {
        session.save(notification);
    }

    public void update(ExamNotification notification) {
        session.update(notification);
    }

    public List<ExamNotification> findUnsentNotifications() {
        String hql = "FROM ExamNotification WHERE isSent = false AND sendTime <= :currentTime";
        return session.createQuery(hql, ExamNotification.class)
                .setParameter("currentTime", new Date())
                .getResultList();
    }

    public List<ExamNotification> findPendingNotifications() {
        String hql = "FROM ExamNotification WHERE isSent = false AND sendTime > :currentTime";
        return session.createQuery(hql, ExamNotification.class)
                .setParameter("currentTime", new Date())
                .getResultList();
    }
}