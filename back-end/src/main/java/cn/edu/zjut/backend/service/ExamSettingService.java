package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamSettingDAO;
import cn.edu.zjut.backend.po.ExamSetting;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("examSettingServ")
public class ExamSettingService {

    @Autowired
    private ExamSettingDAO examSettingDAO;

    // 获取Session（复用工具类）
    public Session getSession() {
        return HibernateUtil.getSession();
    }

    /**
     * 校验并保存设置（核心修复：添加Session/事务管理）
     */
    public boolean validateAndSave(ExamSetting setting) {
        Session session = null;
        Transaction tx = null;
        try {
            // 1. 获取有效Session并开启事务
            session = getSession();
            tx = session.beginTransaction();

            // 2. 参数校验逻辑（合并重复的validateSetting方法，避免冗余）
            if (!validateSetting(setting)) {
                return false;
            }

            // 3. 调用DAO保存（手动传入Session，解决空指针）
            examSettingDAO.saveSetting(session, setting);

            // 4. 提交事务
            tx.commit();
            return true;
        } catch (Exception e) {
            // 回滚事务
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            // 打印真实异常，便于排查
            e.printStackTrace();
            throw new RuntimeException("保存考试设置失败：" + e.getMessage());
        } finally {
            // 关闭Session（释放资源）
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }

    /**
     * 校验设置合法性（公开方法，供其他Service调用）
     */
    public boolean validateSetting(ExamSetting setting) {
        // 考试时长必须大于0（强制校验，不能为空）
        if (setting.getDurationMinute() == null || setting.getDurationMinute() <= 0) {
            System.out.println("[校验失败] 考试时长必须>0");
            return false;
        }

        // 及格分数范围校验（0-100，不为空时）
        if (setting.getPassingScore() != null) {
            if (setting.getPassingScore().compareTo(BigDecimal.ZERO) < 0
                    || setting.getPassingScore().compareTo(new BigDecimal(100)) > 0) {
                System.out.println("[校验失败] 及格分数必须在0-100之间");
                return false;
            }
        }

        // 多选题部分得分比例校验（0-1，强制校验，不能为空）
        if (setting.getMultiChoicePartialRatio() == null
                || setting.getMultiChoicePartialRatio().compareTo(BigDecimal.ZERO) < 0
                || setting.getMultiChoicePartialRatio().compareTo(BigDecimal.ONE) > 0) {
            System.out.println("[校验失败] 多选题部分得分比例必须在0-1之间且不能为空");
            return false;
        }

        return true;
    }

    /**
     * 根据考试ID查询设置（优化Session管理）
     */
    public ExamSetting getSettingByExamId(Long examId) {
        Session session = null;
        try {
            session = getSession();
            // 调用DAO时传入Session
            return examSettingDAO.getByExamId(session, examId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询考试设置失败：" + e.getMessage());
        } finally {
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }

    /**
     * 删除考试设置（补充方法，完整CRUD）
     */
    public void deleteSetting(Long examId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = getSession();
            tx = session.beginTransaction();
            examSettingDAO.deleteSetting(session, examId);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("删除考试设置失败：" + e.getMessage());
        } finally {
            if (session != null) {
                HibernateUtil.closeSession();
            }
        }
    }
}