package cn.edu.zjut.backend.dao;

import cn.edu.zjut.backend.po.ExamCourse;
import cn.edu.zjut.backend.po.ExamCoursePK;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExamCourseDAO {
    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * 添加考试到课程
     * @param examCourse 考试课程关联对象
     * @return 是否添加成功
     */
    public boolean addExamToCourse(ExamCourse examCourse) {
        try {
            session.save(examCourse);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据考试ID删除考试在所有课程中的发布
     * @param examId 考试ID
     * @return 是否删除成功
     */
    public boolean deleteByExamId(Long examId) {
        try {
            String hql = "DELETE FROM ExamCourse WHERE id.examId = :examId";
            Query query = session.createQuery(hql);
            query.setParameter("examId", examId);
            int result = query.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据课程ID删除考试在该课程中的发布
     * @param courseId 课程ID
     * @return 是否删除成功
     */
    public boolean deleteByCourseId(Long courseId) {
        try {
            String hql = "DELETE FROM ExamCourse WHERE id.courseId = :courseId";
            Query query = session.createQuery(hql);
            query.setParameter("courseId", courseId);
            int result = query.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据考试ID和课程ID删除考试在指定课程中的发布
     * @param examId 考试ID
     * @param courseId 课程ID
     * @return 是否删除成功
     */
    public boolean deleteByExamIdAndCourseId(Long examId, Long courseId) {
        try {
            ExamCoursePK id = new ExamCoursePK(examId, courseId);
            session.delete(session.get(ExamCourse.class, id));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据考试ID获取所有关联的课程
     * @param examId 考试ID
     * @return 课程ID列表
     */
    public List<Long> getCoursesByExamId(Long examId) {
        try {
            String hql = "SELECT id.courseId FROM ExamCourse WHERE id.examId = :examId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("examId", examId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据课程ID获取所有关联的考试
     * @param courseId 课程ID
     * @return 考试ID列表
     */
    public List<Long> getExamsByCourseId(Long courseId) {
        try {
            String hql = "SELECT id.examId FROM ExamCourse WHERE id.courseId = :courseId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("courseId", courseId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 检查考试是否已发布到指定课程
     * @param examId 考试ID
     * @param courseId 课程ID
     * @return 是否已发布
     */
    public boolean isExamPublishedToCourse(Long examId, Long courseId) {
        try {
            String hql = "SELECT COUNT(*) FROM ExamCourse WHERE id.examId = :examId AND id.courseId = :courseId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("examId", examId);
            query.setParameter("courseId", courseId);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}