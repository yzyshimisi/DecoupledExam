package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.SubjectDAO;
import cn.edu.zjut.backend.dto.SubjectQueryDTO;
import cn.edu.zjut.backend.po.Subject;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("subjectServ")
public class SubjectService {

    public Session getSession() {
        // SessionFactory sf= new Configuration().configure().buildSessionFactory();
        // return sf.openSession();

        return HibernateUtil.getSession();
    }

    public boolean addSubject(Subject subject) {
        Session session = getSession();
        SubjectDAO dao = new SubjectDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            dao.add(subject);
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

    // 支持一个数组创建科目
    public boolean addSubject(List<Subject> subject) {
        Session session = getSession();
        SubjectDAO dao = new SubjectDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for(Subject s : subject) {
                dao.add(s);
            }
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

    public List<Subject> getSubject(SubjectQueryDTO filterDTO) {
        Session session = getSession();
        SubjectDAO dao = new SubjectDAO();
        dao.setSession(session);
        List<Subject> subjects = dao.query(filterDTO);
        HibernateUtil.closeSession();
        return subjects;
    }

    public boolean deleteSubject(List<Integer> ids) {
        Session session = getSession();
        SubjectDAO dao = new SubjectDAO();
        dao.setSession(session);
        Transaction tran = null;
        try {
            tran = session.beginTransaction();
            for (Integer id : ids) {
                dao.delete(id);
            }
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
