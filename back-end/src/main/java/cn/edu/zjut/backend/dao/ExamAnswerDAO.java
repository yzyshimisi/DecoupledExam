package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamAnswer;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import java.util.List;

public class ExamAnswerDAO {
    private final Log log = LogFactory.getLog(ExamAnswerDAO.class);
    @Setter
    private Session session;

    public void save(ExamAnswer examAnswer) {
        log.debug("saving examAnswer instance");
        try {
            session.save(examAnswer);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        } finally{
        }
    }

    public List<ExamAnswer> queryByRecordId(Long recordId) {
        return null;
    }
}
