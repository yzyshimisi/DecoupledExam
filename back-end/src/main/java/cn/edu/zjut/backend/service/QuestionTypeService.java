package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.QuestionTypeDAO;
import cn.edu.zjut.backend.po.QuestionType;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("questionTypeServ")
public class QuestionTypeService {

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    public List<QuestionType> getAllQuestionTypes() {
        Session session = getSession();
        QuestionTypeDAO dao = new QuestionTypeDAO();
        dao.setSession(session);

        List<QuestionType> questionTypes = dao.queryAll();

        HibernateUtil.closeSession();
        return questionTypes;
    }
}
