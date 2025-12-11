package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.QuestionDAO;
import cn.edu.zjut.backend.dto.QuestionQueryDTO;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("questionServ")
public class QuestionService {

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    // 添加题目
    public boolean addQuestion(Questions question){
        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            dao.add(question);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("save customer failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    // 查询题目
    public List<Questions> queryQuestion(QuestionQueryDTO dto){
        Session session = getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        return dao.query(dto);
    }

    // 删除题目，支持批量删除
    public boolean deleteQuestion(List<Long> ids){
        Session session=this.getSession();
        QuestionDAO dao = new QuestionDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            dao.delete(ids);
            tran.commit();
            return true;
        } catch (Exception e) {
            System.out.println("save customer failed "+ e);
            if (tran != null) {
                tran.rollback();
            }
            return false;
        } finally {
            HibernateUtil.closeSession();
        }
    }
}
