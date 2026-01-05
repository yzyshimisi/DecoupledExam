package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.StudentGradeDAO;
import cn.edu.zjut.backend.po.StudentGrade;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentGradeService {

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    public boolean addStudentGrade(StudentGrade studentGrade) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        Transaction transaction = null;
        try {
            System.out.println("开始添加成绩: " + studentGrade.toString());
            transaction = session.beginTransaction();
            dao.add(studentGrade);
            transaction.commit();
            System.out.println("成绩添加成功");
            return true;
        } catch (Exception e) {
            System.err.println("添加成绩时发生异常: " + e.getMessage());
            e.printStackTrace();

            if (transaction != null) {
                transaction.rollback();
            }
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public boolean updateStudentGrade(StudentGrade studentGrade) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            dao.update(studentGrade);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public boolean deleteStudentGrade(Long gradeId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            dao.delete(gradeId);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public StudentGrade getStudentGradeById(Long gradeId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getById(gradeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getStudentGradesByStudentId(Long studentId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getByStudentId(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getStudentGradesByCourseId(Long courseId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getByCourseId(courseId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getStudentGradesByGradeType(String gradeType) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getByGradeType(gradeType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getAllStudentGrades() {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getStudentGradesByStudentIdAndCourseId(Long studentId, Long courseId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getByStudentIdAndCourseId(studentId, courseId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public List<StudentGrade> getStudentGradesByTeacher(Long teacherId) {
        Session session = getSession();
        StudentGradeDAO dao = new StudentGradeDAO();
        dao.setSession(session);
        try {
            return dao.getByTeacherId(teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

}