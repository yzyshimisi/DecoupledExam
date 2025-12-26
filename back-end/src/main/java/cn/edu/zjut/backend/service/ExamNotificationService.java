package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamNotificationDAO;
import cn.edu.zjut.backend.dao.ExamDAO;
import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamNotification;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Calendar;
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

    // 定时器，用于定期检查和发送通知
    private Timer notificationTimer;

    @PostConstruct
    public void init() {
        // 初始化定时器，每分钟检查一次是否有需要发送的通知
        notificationTimer = new Timer("ExamNotificationTimer", true);
        notificationTimer.scheduleAtFixedRate(new NotificationTask(), 0, 60 * 1000); // 每分钟执行一次
        System.out.println("考试通知定时任务已启动");
    }

    /**
     * 为考试创建通知（在考试前10分钟发送）
     *
     * @param exam 考试对象
     */
    public void createPreExamNotification(Exam exam) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            notificationDAO.setSession(session);
            examDAO.setSession(session);

            // 创建考试前10分钟的通知
            Calendar cal = Calendar.getInstance();
            cal.setTime(exam.getStartTime());
            cal.add(Calendar.MINUTE, -10); // 考试开始前10分钟

            ExamNotification notification = new ExamNotification();
            notification.setExamId(exam.getExamId());
            notification.setTitle("考试提醒");
            notification.setContent("您有一场考试即将开始：《" + exam.getTitle() + "》，请做好准备。");
            notification.setSendTime(cal.getTime());
            notification.setIsSent(false);
            notification.setSendAttempts(0);

            notificationDAO.save(notification);
            System.out.println("已为考试ID " + exam.getExamId() + " 创建通知，计划发送时间: " + cal.getTime());

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
     * 为考试创建通知（在考试前10分钟发送）- 重载版本，接受外部传入的Session
     *
     * @param exam 考试对象
     * @param session Hibernate Session
     */
    public void createPreExamNotification(Exam exam, Session session) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();

            notificationDAO.setSession(session);
            examDAO.setSession(session);

            // 创建考试前10分钟的通知
            Calendar cal = Calendar.getInstance();
            cal.setTime(exam.getStartTime());
            cal.add(Calendar.MINUTE, -10); // 考试开始前10分钟

            ExamNotification notification = new ExamNotification();
            notification.setExamId(exam.getExamId());
            notification.setTitle("考试提醒");
            notification.setContent("您有一场考试即将开始：《" + exam.getTitle() + "》，请做好准备。");
            notification.setSendTime(cal.getTime());
            notification.setIsSent(false);
            notification.setSendAttempts(0);

            notificationDAO.save(notification);
            System.out.println("已为考试ID " + exam.getExamId() + " 创建通知，计划发送时间: " + cal.getTime());

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        } finally {
            // 不要关闭外部传入的session
        }
    }

    /**
     * 发送通知的方法（模拟实现）
     *
     * @param notification 通知对象
     */
    private void sendNotification(ExamNotification notification) {
        // 这里应该集成真正的消息推送服务，如邮件、短信、站内信等
        // 当前仅为模拟实现
        System.out.println("发送通知 - 标题: " + notification.getTitle() +
                          ", 内容: " + notification.getContent() +
                          ", 考试ID: " + notification.getExamId());

        // 更新通知状态
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            notificationDAO.setSession(session);

            notification.setIsSent(true);
            notification.setSendAttempts(notification.getSendAttempts() + 1);

            notificationDAO.update(notification);

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
     * 定时任务类，用于检查和发送通知
     */
    private class NotificationTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("检查待发送通知: " + new Date());
            Session session = null;
            try {
                session = HibernateUtil.getSession();
                notificationDAO.setSession(session);

                // 查找需要发送的通知
                List<ExamNotification> unsentNotifications = notificationDAO.findUnsentNotifications();
                System.out.println("找到 " + unsentNotifications.size() + " 条待发送通知");

                // 发送每个通知
                for (ExamNotification notification : unsentNotifications) {
                    sendNotification(notification);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Session将在HibernateUtil的ThreadLocal中管理
            }
        }
    }

    // 销毁时关闭定时器
    public void destroy() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
    }
}