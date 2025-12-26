package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamNotification;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

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

    public List<ExamNotification> findNotificationsByStudentId(Long studentId) {
        String hql = "FROM ExamNotification WHERE studentId = :studentId ORDER BY sendTime DESC";
        return session.createQuery(hql, ExamNotification.class)
                .setParameter("studentId", studentId)
                .getResultList();
    }
    
    public List<ExamNotification> findNotificationsByStudentIdAndExamId(Long studentId, Long examId) {
        String hql = "FROM ExamNotification WHERE studentId = :studentId AND examId = :examId ORDER BY sendTime DESC";
        return session.createQuery(hql, ExamNotification.class)
                .setParameter("studentId", studentId)
                .setParameter("examId", examId)
                .getResultList();
    }
    
    public List<ExamNotification> findNotificationsByExamId(Long examId) {
        String hql = "FROM ExamNotification WHERE examId = :examId ORDER BY sendTime DESC";
        return session.createQuery(hql, ExamNotification.class)
                .setParameter("examId", examId)
                .getResultList();
    }
}