package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExamRecordDAO {
    private final Log log = LogFactory.getLog(ExamRecordDAO.class);
    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加考生到考试（创建考试记录）
     * @param session Hibernate会话
     * @param record 考试记录
     */
    public void addExamRecord(Session session, ExamRecord record) {
        log.debug("saving exam record instance");
        try {
            session.save(record);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    /**
     * 批量添加考生到考试
     * @param session Hibernate会话
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     */
    public void addStudentsToExam(Session session, Long examId, List<Long> studentIds) {
        try {
            for (Long studentId : studentIds) {
                // 检查是否已存在记录
                if (!isStudentInExam(session, examId, studentId)) {
                    ExamRecord record = new ExamRecord(examId, studentId);
                    session.save(record);
                }
            }
            log.debug("批量添加考生成功");
        } catch (RuntimeException re) {
            log.error("批量添加考生失败", re);
            throw re;
        }
    }

    /**
     * 检查学生是否已在考试中
     * @param session Hibernate会话
     * @param examId 考试ID
     * @param studentId 学生ID
     * @return 是否存在
     */
    public boolean isStudentInExam(Session session, Long examId, Long studentId) {
        try {
            String hql = "select count(r.recordId) from ExamRecord r where r.examId = :examId and r.studentId = :studentId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("examId", examId);
            query.setParameter("studentId", studentId);
            return query.uniqueResult() > 0;
        } catch (RuntimeException re) {
            log.error("检查学生是否在考试中失败", re);
            throw re;
        }
    }

    /**
     * 从考试中移除考生
     * @param session Hibernate会话
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     */
    public void removeStudentsFromExam(Session session, Long examId, List<Long> studentIds) {
        try {
            String hql = "delete from ExamRecord r where r.examId = :examId and r.studentId in (:studentIds)";
            Query query = session.createQuery(hql);
            query.setParameter("examId", examId);
            query.setParameterList("studentIds", studentIds);
            int deletedCount = query.executeUpdate();
            log.debug("批量移除考生成功，共删除 " + deletedCount + " 条记录");
        } catch (RuntimeException re) {
            log.error("批量移除考生失败", re);
            throw re;
        }
    }

    /**
     * 获取考试的所有考生记录
     * @param session Hibernate会话
     * @param examId 考试ID
     * @return 考试记录列表
     */
    public List<ExamRecord> getRecordsByExamId(Session session, Long examId) {
        try {
            String hql = "from ExamRecord r where r.examId = :examId";
            Query<ExamRecord> query = session.createQuery(hql, ExamRecord.class);
            query.setParameter("examId", examId);
            return query.list();
        } catch (RuntimeException re) {
            log.error("查询考试记录失败", re);
            throw re;
        }
    }
    
    /**
     * 根据考试记录ID获取考试记录
     * @param session Hibernate会话
     * @param recordId 考试记录ID
     * @return 考试记录
     */
    public ExamRecord getRecordById(Session session, Long recordId) {
        try {
            String hql = "from ExamRecord r where r.recordId = :recordId";
            Query<ExamRecord> query = session.createQuery(hql, ExamRecord.class);
            query.setParameter("recordId", recordId);
            return query.uniqueResult();
        } catch (RuntimeException re) {
            log.error("查询考试记录失败", re);
            throw re;
        }
    }

    public void updateExamRecord(Session session, ExamRecord record) {
        try {
            session.save(record);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }
}