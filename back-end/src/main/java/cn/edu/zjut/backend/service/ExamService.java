package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamDAO;
import cn.edu.zjut.backend.dao.ExamSettingDAO;
import cn.edu.zjut.backend.dao.ExamRecordDAO;
import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamSetting;
import cn.edu.zjut.backend.po.ExamRecord;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("examServ")
public class ExamService {

    @Autowired
    private ExamDAO examDAO;

    @Autowired
    private ExamSettingDAO examSettingDAO;
    
    @Autowired
    private ExamRecordDAO examRecordDAO;
    
    @Autowired
    private ExamNotificationService examNotificationService;

    /**
     * 创建考试（核心修复：先保存Exam获取ID，再保存ExamSetting）
     */
    public Long createExam(Exam exam, ExamSetting setting) {
        Session session = null;
        Transaction tran = null;
        try {
            session = HibernateUtil.getSession();
            tran = session.beginTransaction();

            // 步骤1：保存Exam（生成自增examId）
            examDAO.setSession(session);
            session.save(exam);
            session.flush(); // 强制刷新以获取生成的ID
            Long examId = exam.getExamId(); // 获取自增ID

            // 步骤2：绑定examId到ExamSetting，补充默认值（匹配SQL）
            setting.setExamId(examId);
            // 补充SQL默认值（防止字段为空）
            if (setting.getAllowLateEnter() == null) setting.setAllowLateEnter((byte) 1);
            if (setting.getQuestionShuffle() == null) setting.setQuestionShuffle((byte) 1);
            if (setting.getOptionShuffle() == null) setting.setOptionShuffle((byte) 1);
            if (setting.getPreventScreenSwitch() == null) setting.setPreventScreenSwitch((byte) 0);
            if (setting.getAutoSubmit() == null) setting.setAutoSubmit((byte) 1);
            if (setting.getAllowViewPaper() == null) setting.setAllowViewPaper((byte) 1);
            if (setting.getAllowViewScore() == null) setting.setAllowViewScore((byte) 1);
            if (setting.getGenerateExamCode() == null) setting.setGenerateExamCode((byte) 0);
            if (setting.getPeerReview() == null) setting.setPeerReview((byte) 0);
            if (setting.getFillCaseSensitive() == null) setting.setFillCaseSensitive((byte) 0);
            if (setting.getFillIgnoreSymbols() == null) setting.setFillIgnoreSymbols((byte) 1);
            if (setting.getFillManualMark() == null) setting.setFillManualMark((byte) 0);
            if (setting.getMultiChoicePartialScore() == null) setting.setMultiChoicePartialScore((byte) 1);
            if (setting.getSortQuestionScorePerBlank() == null) setting.setSortQuestionScorePerBlank((byte) 1);

            // 步骤3：保存ExamSetting
            examSettingDAO.saveSetting(session, setting);
            
            // 步骤4：创建考试通知（在考试开始前10分钟提醒）
            examNotificationService.createPreExamNotification(exam, session);

            tran.commit();
            return examId;
        } catch (Exception e) {
            if (tran != null) tran.rollback();
            e.printStackTrace();
            return null;
        } finally {
            // 不要在创建通知之前关闭session
            // Session将在HibernateUtil的ThreadLocal中管理
        }
    }

    /**
     * 查询考试列表
     * @param status 考试状态 (可选)
     * @param teacherId 教师ID (可选)
     * @return 考试列表
     */
    public List<Exam> getExamList(Byte status, Long teacherId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            return examDAO.queryByStatus(status, teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试列表失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 根据ID获取考试详情
     * @param examId 考试ID
     * @return 考试对象
     */
    public Exam getExamById(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            return examDAO.getById(examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试详情失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 更新考试信息
     * @param exam 考试对象
     * @return 是否更新成功
     */
    public boolean updateExam(Exam exam) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            examDAO.setSession(session);
            examDAO.updateExam(exam);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("更新考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 删除考试
     * @param examId 考试ID
     * @return 是否删除成功
     */
    public boolean deleteExam(Long examId) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            examDAO.setSession(session);
            examDAO.deleteExam(examId);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("删除考试失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 为考试添加学生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否添加成功
     */
    public boolean addStudentsToExam(Long examId, List<Long> studentIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            
            // 检查考试是否存在
            examDAO.setSession(session);
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }
            
            // 添加学生到考试
            examRecordDAO.addStudentsToExam(session, examId, studentIds);
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("添加考生失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 从考试中移除学生
     * @param examId 考试ID
     * @param studentIds 学生ID列表
     * @return 是否移除成功
     */
    public boolean removeStudentsFromExam(Long examId, List<Long> studentIds) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            
            // 检查考试是否存在
            examDAO.setSession(session);
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }
            
            // 从考试中移除学生
            examRecordDAO.removeStudentsFromExam(session, examId, studentIds);
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("移除考生失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 获取考试的所有考生记录
     * @param examId 考试ID
     * @return 考试记录列表
     */
    public List<ExamRecord> getStudentsByExam(Long examId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            examDAO.setSession(session);
            // 先检查考试是否存在
            Exam exam = examDAO.getById(examId);
            if (exam == null) {
                throw new RuntimeException("考试不存在");
            }
            return examRecordDAO.getRecordsByExamId(session, examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考生列表失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    /**
     * 根据考试记录ID获取考试记录
     * @param recordId 考试记录ID
     * @return 考试记录
     */
    public ExamRecord getExamRecordById(Long recordId) {
        Session session = null;
        try {
            session = HibernateUtil.getSession();
            return examRecordDAO.getRecordById(session, recordId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试记录失败：" + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}