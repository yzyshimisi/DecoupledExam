package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.StudentGrade;
import cn.edu.zjut.backend.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class StudentGradeDAO {
    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    public void add(StudentGrade studentGrade) {
        System.out.println("DAO层开始添加成绩: " + studentGrade.toString());
        session.save(studentGrade);
        System.out.println("DAO层添加完成，生成的ID: " + studentGrade.getGradeId());
    }

    public void update(StudentGrade studentGrade) {
        session.update(studentGrade);
    }

    public void delete(Long gradeId) {
        StudentGrade studentGrade = session.get(StudentGrade.class, gradeId);
        if (studentGrade != null) {
            session.delete(studentGrade);
        }
    }

    public StudentGrade getById(Long gradeId) {
        return session.get(StudentGrade.class, gradeId);
    }

    public List<StudentGrade> getByStudentId(Long studentId) {
        String hql = "FROM StudentGrade WHERE studentId = :studentId ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        query.setParameter("studentId", studentId);
        return query.list();
    }

    public List<StudentGrade> getByCourseId(Long courseId) {
        String hql = "FROM StudentGrade WHERE courseId = :courseId ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        query.setParameter("courseId", courseId);
        return query.list();
    }

    public List<StudentGrade> getByGradeType(String gradeType) {
        String hql = "FROM StudentGrade WHERE gradeType = :gradeType ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        query.setParameter("gradeType", gradeType);
        return query.list();
    }

    public List<StudentGrade> getAll() {
        String hql = "FROM StudentGrade ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        return query.list();
    }

    public List<StudentGrade> getByStudentIdAndCourseId(Long studentId, Long courseId) {
        String hql = "FROM StudentGrade WHERE studentId = :studentId AND courseId = :courseId ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        query.setParameter("studentId", studentId);
        query.setParameter("courseId", courseId);
        return query.list();
    }

    public List<StudentGrade> getByTeacherId(Long teacherId) {
        String hql = "FROM StudentGrade WHERE courseId IN (SELECT courseId FROM Course WHERE teacherId = :teacherId) ORDER BY recordTime DESC";
        Query<StudentGrade> query = session.createQuery(hql, StudentGrade.class);
        query.setParameter("teacherId", teacherId);
        return query.list();
    }
}