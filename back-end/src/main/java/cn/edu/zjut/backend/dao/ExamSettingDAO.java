package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamSetting;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ExamSettingDAO {
    private final Log log = LogFactory.getLog(ExamSettingDAO.class);
    // 移除本地Session变量，改为方法参数传入（避免空指针）
    // private Session session;

    // 移除setSession方法，改为手动传入Session
    // public void setSession(Session session) {
    //     this.session = session;
    // }

    // 保存/更新考试设置（新增Session参数）
    public void saveSetting(Session session, ExamSetting setting) {
        log.debug("saving exam setting instance");
        try {
            // 存在settingId则更新，否则新增
            if (setting.getSettingId() != null) {
                session.update(setting);
            } else {
                session.save(setting);
            }
            log.debug("save or update successful");
        } catch (RuntimeException re) {
            log.error("save or update failed", re);
            throw re;
        }
    }

    // 按考试ID查询设置（新增Session参数）
    public ExamSetting getByExamId(Session session, Long examId) {
        try {
            String hql = "from ExamSetting es where es.examId = :examId";
            Query<ExamSetting> query = session.createQuery(hql, ExamSetting.class);
            query.setParameter("examId", examId);
            return query.uniqueResult();
        } catch (RuntimeException re) {
            log.error("get setting failed", re);
            throw re;
        }
    }

    // 补充：删除设置（新增Session参数）
    public void deleteSetting(Session session, Long examId) {
        try {
            ExamSetting setting = getByExamId(session, examId);
            if (setting != null) {
                session.delete(setting);
            }
        } catch (RuntimeException re) {
            log.error("delete setting failed", re);
            throw re;
        }
    }
}