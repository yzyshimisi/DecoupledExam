package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamAnswerDAO;
import cn.edu.zjut.backend.dao.ExamRecordDAO;
import cn.edu.zjut.backend.dao.ExamWrongBookDAO;
import cn.edu.zjut.backend.dao.QuestionDAO;
import cn.edu.zjut.backend.dto.ExamWrongBookDTO;
import cn.edu.zjut.backend.po.ExamAnswer;
import cn.edu.zjut.backend.po.ExamRecord;
import cn.edu.zjut.backend.po.ExamWrongBook;
import cn.edu.zjut.backend.po.Questions;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("examWrongBookServ")
public class ExamWrongBookService {

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    public List<ExamWrongBookDTO> queryExamWrongBook(Long studentId) throws Exception{

        Session session = getSession();
        ExamWrongBookDAO examWrongBookDAO = new ExamWrongBookDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamAnswerDAO examAnswerDAO = new ExamAnswerDAO();
        examWrongBookDAO.setSession(session);
        questionDAO.setSession(session);
        examAnswerDAO.setSession(session);

        List<ExamWrongBookDTO> examWrongBookDTOs = new ArrayList<>();

        try{
            List<ExamWrongBook> examWrongBookList = examWrongBookDAO.queryByStudentId(studentId);   // 获取错题本

            for(ExamWrongBook examWrongBook : examWrongBookList){   // 获取每一道题目
                Questions questions = questionDAO.query(examWrongBook.getQuestionId());

                ExamWrongBookDTO examWrongBookDTO = new ExamWrongBookDTO();
                examWrongBookDTO.setExamWrongBook(examWrongBook);
                examWrongBookDTO.setQuestions(questions);

                examWrongBookDTOs.add(examWrongBookDTO);
            }

            return examWrongBookDTOs;
        }finally {
            HibernateUtil.closeSession();
        }
    }


    public boolean handleExamWrongBook(List<ExamWrongBook> examWrongBookList, String operation) throws Exception{
        Session session = this.getSession();
        ExamWrongBookDAO dao = new ExamWrongBookDAO();
        dao.setSession(session);
        Transaction tran = null;

        try {
            tran = session.beginTransaction();

            for (ExamWrongBook examWrongBook : examWrongBookList) {
                switch (operation) {
                    case "add":
                        dao.save(examWrongBook);
                        break;
                    case "delete":
                        dao.delete(examWrongBook);
                        break;
                    case "update":
                        dao.update(examWrongBook);
                        break;
                }
            }

            tran.commit();
            return true;
        } catch (Exception e) {
            if (tran != null) {
                tran.rollback();
            }
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public ExamWrongBook queryExamWrongBookById(Long id) throws Exception{
        Session session = this.getSession();
        ExamWrongBookDAO dao = new ExamWrongBookDAO();
        dao.setSession(session);

        try{
            return dao.queryById(id);
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public List<ExamWrongBook> queryExamWrongBookByStudentId(Long studentId) throws Exception{
        Session session = this.getSession();
        ExamWrongBookDAO dao = new ExamWrongBookDAO();
        dao.setSession(session);

        try{
            return dao.queryByStudentId(studentId);
        } finally {
            HibernateUtil.closeSession();
        }
    }
}
