package cn.edu.zjut.backend.service;

import cn.edu.zjut.backend.dao.StudentGradeDAO;
import cn.edu.zjut.backend.po.StudentGrade;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Date;

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
            System.out.println("开始更新成绩: " + studentGrade.toString());

            // 验证必要字段
            if (studentGrade == null) {
                System.err.println("传入的成绩对象为空");
                return false;
            }

            if (studentGrade.getGradeId() == null) {
                System.err.println("gradeId不能为空");
                return false;
            }

            if (studentGrade.getStudentId() == null) {
                System.err.println("studentId不能为空");
                return false;
            }

            if (studentGrade.getGradeType() == null || studentGrade.getGradeType().trim().isEmpty()) {
                System.err.println("gradeType不能为空");
                return false;
            }

            if (studentGrade.getGradeName() == null || studentGrade.getGradeName().trim().isEmpty()) {
                System.err.println("gradeName不能为空");
                return false;
            }

            if (studentGrade.getScore() == null) {
                System.err.println("score不能为空");
                return false;
            }

            if (studentGrade.getFullScore() == null) {
                System.err.println("fullScore不能为空");
                return false;
            }

            if (studentGrade.getTeacherId() == null) {
                System.err.println("teacherId不能为空");
                return false;
            }

            // 设置recordTime为当前时间（如果为空）
            if (studentGrade.getRecordTime() == null) {
                studentGrade.setRecordTime(new Date());
                System.out.println("recordTime已自动设置为当前时间");
            }

            // 开始事务
            transaction = session.beginTransaction();

            // 执行更新
            dao.update(studentGrade);

            // 提交事务
            transaction.commit();
            System.out.println("成绩更新成功，ID: " + studentGrade.getGradeId());
            return true;

        } catch (Exception e) {
            // 记录详细的错误信息
            System.err.println("更新成绩时发生异常: " + e.getMessage());
            e.printStackTrace();

            // 回滚事务
            if (transaction != null) {
                try {
                    transaction.rollback();
                    System.err.println("事务已回滚");
                } catch (Exception rollbackEx) {
                    System.err.println("事务回滚失败: " + rollbackEx.getMessage());
                }
            }

            return false;

        } finally {
            // 关闭会话
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                    System.out.println("数据库会话已关闭");
                } catch (Exception closeEx) {
                    System.err.println("关闭会话失败: " + closeEx.getMessage());
                }
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