package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.ExamAnswerDAO;
import cn.edu.zjut.backend.dao.ExamDAO;
import cn.edu.zjut.backend.dao.ExamRecordDAO;
import cn.edu.zjut.backend.dto.AddExamAnswerDTO;
import cn.edu.zjut.backend.po.Exam;
import cn.edu.zjut.backend.po.ExamAnswer;
import cn.edu.zjut.backend.po.ExamRecord;
import cn.edu.zjut.backend.util.ExamContext;
import cn.edu.zjut.backend.util.HibernateUtil;
import cn.edu.zjut.backend.util.Jwt;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("examAnswerServ")
public class ExamAnswerService {
    public Session getSession() {

        return HibernateUtil.getSession();
    }

    public boolean addExamAnswer(AddExamAnswerDTO addExamAnswerDTO) throws Exception {

        Map<Long, Object> answers = addExamAnswerDTO.getAnswers();
        Long examId = addExamAnswerDTO.getExamId();

        Session session = this.getSession();
        ExamAnswerDAO examAnswerDAO = new ExamAnswerDAO();
        ExamRecordDAO examRecordDAO = new ExamRecordDAO();
        ExamDAO examDAO = new ExamDAO();
        examDAO.setSession(session);
        examAnswerDAO.setSession(session);

        Transaction tran = null;

        try {
            tran = session.beginTransaction();

            // 是否超时提交
            Exam exam = examDAO.getById(examId);
            Date now = new Date();  // 获取当前时间
            Date deadline = new Date(exam.getEndTime().getTime() + 3000);   // 考试结束时间多加3秒
            if(now.after(deadline)) throw new Exception("不在可提交时间范围内");

            for (Map.Entry<Long, Object> entry : answers.entrySet()) {
                Long questionId = entry.getKey();   // 题目ID
                Object value = entry.getValue();    // 对应的答案

                List<ExamRecord> examRecords = examRecordDAO.getRecordsByExamId(session, examId);   // 获取对应的记录
                ExamRecord examRecord = null;
                for (ExamRecord e : examRecords) {
                    if(e.getStudentId().equals(ExamContext.getStudentId())){

                        if(e.getStatus().equals("1")){  // 已经提交了（系统强制提交后，用户不可再次提交）
                            return false;
                        }

                        examRecord = e;
                        break;
                    }
                }

                Gson gson = new Gson();

                ExamAnswer examAnswer = new ExamAnswer();
                examAnswer.setRecordId(examRecord.getRecordId());
                examAnswer.setQuestionId(questionId);
                examAnswer.setMyAnswer(gson.toJson(value));
                examAnswer.setMarkType("1");

                examAnswerDAO.save(examAnswer);
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
}
