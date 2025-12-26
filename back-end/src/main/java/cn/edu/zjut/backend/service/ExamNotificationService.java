package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamNotificationDAO;
import cn.edu.zjut.backend.dao.ExamDAO;
import cn.edu.zjut.backend.dao.ExamCourseDAO;
import cn.edu.zjut.backend.dao.StudentCourseDAO;
import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamNotification;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service("examNotificationService")
public class ExamNotificationService {

    @Autowired
    private ExamNotificationDAO notificationDAO;

    @Autowired
    private ExamDAO examDAO;
    
    @Autowired
    private ExamCourseDAO examCourseDAO;
    
    @Autowired
    private StudentCourseDAO studentCourseDAO;

    // 定时器，用于定期检查和发送通知（现在不再使用时间相关通知）
    private Timer notificationTimer;

    @PostConstruct
    public void init() {
        // 初始化定时器，每分钟检查一次是否有需要发送的通知
        // 注意：现在只保留定时器结构，不执行任何操作，因为所有通知都是即时发送
        notificationTimer = new Timer("ExamNotificationTimer", true);
        System.out.println("考试通知服务已启动（仅用于兼容性，无定时任务）");
    }

    /**
     * 为考试创建即时通知（在发布考试时直接发送给相关学生）
     *
     * @param exam 考试对象
     * @param courseIds 课程ID列表
     */
    public void createImmediateNotificationForExam(Exam exam, List<Long> courseIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            notificationDAO.setSession(session);
            examCourseDAO.setSession(session);
            studentCourseDAO.setSession(session);
            
            // 为每个课程的学生创建通知
            for (Long courseId : courseIds) {
                // 获取该课程的所有学生
                List<Long> studentIds = studentCourseDAO.getStudentsByCourseId(courseId);
                
                if (studentIds != null && !studentIds.isEmpty()) {
                    for (Long studentId : studentIds) {
                        ExamNotification notification = new ExamNotification();
                        notification.setExamId(exam.getExamId());
                        notification.setStudentId(studentId); // 指定发送给特定学生
                        notification.setTitle("考试通知");
                        notification.setContent("考试《" + exam.getTitle() + "》即将开始，请同学们做好准备。");
                        notification.setSendTime(new Date()); // 立即发送
                        notification.setIsSent(true); // 直接标记为已发送
                        notification.setSendAttempts(1); // 发送次数为1

                        notificationDAO.save(notification);
                        System.out.println("已为学生ID " + studentId + " 创建并发送考试通知，考试ID: " + exam.getExamId());
                    }
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            // Session将在HibernateUtil的ThreadLocal中管理
        }
    }
    
    /**
     * 为现有考试创建通知（为已有考试的学生发送通知）
     *
     * @param exam 考试对象
     */
    public void createNotificationForExistingExam(Exam exam) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            notificationDAO.setSession(session);
            examCourseDAO.setSession(session);
            studentCourseDAO.setSession(session);
            
            // 获取考试相关的课程
            List<Long> courseIds = examCourseDAO.getCoursesByExamId(exam.getExamId());
            
            // 为每个课程的学生创建通知
            for (Long courseId : courseIds) {
                // 获取该课程的所有学生
                List<Long> studentIds = studentCourseDAO.getStudentsByCourseId(courseId);
                
                if (studentIds != null && !studentIds.isEmpty()) {
                    for (Long studentId : studentIds) {
                        // 检查是否已存在通知
                        String hql = "FROM ExamNotification WHERE examId = :examId AND studentId = :studentId";
                        List<ExamNotification> existingNotifications = session.createQuery(hql, ExamNotification.class)
                                .setParameter("examId", exam.getExamId())
                                .setParameter("studentId", studentId)
                                .getResultList();
                        
                        if (existingNotifications.isEmpty()) { // 只有在没有现有通知时才创建
                            ExamNotification notification = new ExamNotification();
                            notification.setExamId(exam.getExamId());
                            notification.setStudentId(studentId); // 指定发送给特定学生
                            notification.setTitle("考试通知");
                            notification.setContent("考试《" + exam.getTitle() + "》即将开始，请同学们做好准备。");
                            notification.setSendTime(new Date()); // 立即发送
                            notification.setIsSent(true); // 直接标记为已发送
                            notification.setSendAttempts(1); // 发送次数为1

                            notificationDAO.save(notification);
                            System.out.println("已为学生ID " + studentId + " 创建并发送考试通知，考试ID: " + exam.getExamId());
                        }
                    }
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            // Session将在HibernateUtil的ThreadLocal中管理
        }
    }
    
    /**
     * 根据学生ID获取通知列表
     *
     * @param studentId 学生ID
     * @return 通知列表
     */
    public List<ExamNotification> getNotificationsByStudentId(Long studentId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            notificationDAO.setSession(session);
            
            return notificationDAO.findNotificationsByStudentId(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询学生通知失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * 根据学生ID和考试ID获取通知
     *
     * @param studentId 学生ID
     * @param examId 考试ID
     * @return 通知列表
     */
    public List<ExamNotification> getNotificationsByStudentIdAndExamId(Long studentId, Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            notificationDAO.setSession(session);
            
            return notificationDAO.findNotificationsByStudentIdAndExamId(studentId, examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询学生考试通知失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * 根据考试ID获取所有通知
     *
     * @param examId 考试ID
     * @return 通知列表
     */
    public List<ExamNotification> getNotificationsByExamId(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            notificationDAO.setSession(session);
            
            return notificationDAO.findNotificationsByExamId(examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试通知失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 根据考试ID删除考试通知
     * @param examId 考试ID
     * @param session Hibernate Session
     */
    public void deleteByExamId(Long examId, Session session) {
        org.hibernate.query.Query deleteQuery = session.createQuery("DELETE FROM ExamNotification WHERE examId = :examId");
        deleteQuery.setParameter("examId", examId);
        int deletedCount = deleteQuery.executeUpdate();
        System.out.println("删除了 " + deletedCount + " 条与考试ID " + examId + " 相关的通知");
    }
    
    // 销毁时关闭定时器
    public void destroy() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }
}